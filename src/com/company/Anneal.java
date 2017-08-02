package com.company;

/*
 * Реализация решения судоку с помошью метода имитации отжига
 */


import java.io.File;
import java.io.IOException;
import java.util.*;

public class Anneal {
    private static Random rnd = new Random();

    private static int[][] read(String input) {
        int[][] matrix = new int[9][9];
        try {
            List<String> list = new ArrayList<>();
            Scanner in = new Scanner(new File(input));
            while (in.hasNextLine()) {
                list.add(in.nextLine());
            }
            list.remove(3);
            list.remove(6);
            for (int i = 0; i < list.size(); i++) {
                StringTokenizer st = new StringTokenizer(list.get(i), " |", false);
                int k = 0;
                while (st.hasMoreTokens()) {
                    matrix[i][k] = Integer.parseInt(String.valueOf(st.nextToken()));
                    k++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matrix;
    }

    private static void print(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            if (i % 3 == 0 && i > 0) {
                System.out.print("--------------------");
                System.out.println();
            }
            for (int j = 0; j < matrix.length; j++) {
                if (j % 3 == 0 && j > 0)
                    System.out.print("| ");
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static int[][] fillSudoku(int[][] matrix) {
        for (int block = 0; block < matrix.length; ++block) {
            int[] corners = Corner(block);
            ArrayList<Integer> val = shuffledList();
            int row = corners[0], col = corners[1];
            for (int i = row; i < row + 3; ++i) {
                for (int j = col; j < col + 3; ++j) {
                    if (matrix[i][j] != 0) // a fixed starting number
                        val.remove(val.indexOf(matrix[i][j]));
                }
            }
            for (int i = row; i < row + 3; ++i) {
                for (int j = col; j < col + 3; ++j) {
                    if (matrix[i][j] == 0) // not occupied
                    {
                        // get value from List
                        int v = val.get(0);
                        matrix[i][j] = v;
                        val.remove(0);
                    }
                }
            }
        }
        return matrix;
    }

    private static int Error(int[][] matrix) {
        int err = 0;
        // assumes blocks are OK (one each 1-9)

        // rows error
        for (int i = 0; i < 9; ++i)  // each row
        {
            int[] counts = new int[9];  // [0] = count of 1s, [1] = count of 2s
            for (int j = 0; j < 9; ++j)  // walk down column of curr row
            {
                int v = matrix[i][j];  // 1 to 9
                ++counts[v - 1];
            }

            for (int k = 0; k < 9; ++k)  // number missing
            {
                if (counts[k] == 0)
                    ++err;
            }

        }  // each row

        // columns error
        for (int j = 0; j < 9; ++j)  // each column
        {
            int[] counts = new int[9];  // [0] = count of 1s, [1] = count of 2s

            for (int i = 0; i < 9; ++i)  // walk down
            {
                int v = matrix[i][j];  // 1 to 9
                ++counts[v - 1];  // counts[0-8]
            }

            for (int k = 0; k < 9; ++k)  // number missing in the colum
            {
                if (counts[k] == 0)
                    ++err;
            }
        } // each column

        return err;
    } // Error

    private static int[] Corner(int block) {
        int r = -1, c = -1;

        if (block == 0 || block == 1 || block == 2)
            r = 0;
        else if (block == 3 || block == 4 || block == 5)
            r = 3;
        else if (block == 6 || block == 7 || block == 8)
            r = 6;

        if (block == 0 || block == 3 || block == 6)
            c = 0;
        else if (block == 1 || block == 4 || block == 7)
            c = 3;
        else if (block == 2 || block == 5 || block == 8)
            c = 6;

        return new int[]{r, c};
    }

    private static ArrayList shuffledList() {
        ArrayList list = new ArrayList();
        for (int i = 1; i <= 9; i++)
            list.add(i);
        Collections.shuffle(list);
        return list;
    }

    private static int[][] DuplicateMatrix(int[][] matrix) {
        int[][] result = CreateMatrix(9);
        for (int i = 0; i < 9; ++i)
            System.arraycopy(matrix[i], 0, result[i], 0, 9);
        return result;
    }

    private static int[][] CreateMatrix(int n) {
        int[][] result = new int[n][];
        for (int i = 0; i < n; ++i)
            result[i] = new int[n];
        return result;
    }

    private static int[][] NeighborMatrix(int[][] original, int[][] matrix) {
        // pick a random 3x3 block,
        // pick two random cells in block
        // swap values

        int[][] result = DuplicateMatrix(matrix);

        int block = rnd.nextInt(9);  // [0,8]
        int[] corners = Corner(block);
        ArrayList<int[]> cells = new ArrayList();
        for (int i = corners[0]; i < corners[0] + 3; ++i) {
            for (int j = corners[1]; j < corners[1] + 3; ++j) {
                if (original[i][j] == 0)  // a non-fixed value
                {
                    cells.add(new int[]{i, j});
                }
            }
        }

        if (cells.size() < 2) {
            return null;
        }

        // pick two. suppose there are 4 possible cells 0,1,2,3
        int k1 = rnd.nextInt(cells.size());  // 0,1,2,3
        int inc = rnd.nextInt(cells.size() - 1) + 1;  // 1,2,3
        int k2 = (k1 + inc) % cells.size();

        int r1 = cells.get(k1)[0];
        int c1 = cells.get(k1)[1];
        int r2 = cells.get(k2)[0];
        int c2 = cells.get(k2)[1];

        int tmp = result[r1][c1];
        result[r1][c1] = result[r2][c2];
        result[r2][c2] = tmp;

        return result;

    } // NeighborMatrix

    private static int[][] annealing(double initialTemp, int[][] matrix, double step, double minTemp, int numIter) {
        double temperature = initialTemp;
        int[][] state = fillSudoku(DuplicateMatrix(matrix));
        int currentEnergy = Error(state);
        int i = 0;

        while (temperature > minTemp && i < numIter) {
            i++;
            int[][] candidate = NeighborMatrix(matrix, state);
            int candidadeEnergy = Error(candidate);
            if (candidadeEnergy < currentEnergy) {
                state = candidate;
                currentEnergy = candidadeEnergy;
            } else if (checkProbability(candidadeEnergy - currentEnergy, temperature)) {
                state = candidate;
                currentEnergy = candidadeEnergy;
            }
            //temperature=initialTemp/Math.log(1+i); //больцмановский отжиг
            //наилучшие результаты
            temperature = temperature * (1 - step); //линейная реализация
            //temperature/=Math.pow(i,(1/1000.0)); //сверхбыстрый отжиг
            if (temperature < minTemp)
                break;
        }
        return state;
    }

    private static boolean checkProbability(int deltaE, double temperature) {
        double p = Math.exp(-deltaE / temperature);
        double val = rnd.nextDouble();
        return val <= p;
    }

    public static void main(String[] args) {
        // write your code here

        int[][] matrix = null;
        int flag = -1;
        System.out.println("Программа для решения Судоку методом отжига");
        do {
            System.out.print("Нажмите 1 для загрузки файла, 0 для использования задачи по умолчанию, 2 для выхода: ");
            Scanner in = new Scanner(System.in);
            if (in.hasNextInt()) {
                flag = Integer.parseInt(in.next());
            }
            if (flag == 1) {
                String filePath;
                System.out.println("Введите полный путь к файлу");
                if (in.hasNext()) {
                    filePath = in.next();
                    matrix = read(filePath);
                }
                String str;
                double initialTemp = 0;
                double step = 0;
                double minTemp = 0;
                int numIter = 0;

                System.out.println("Введите начальную температуру больше нуля");
                while (initialTemp <= 0) {
                    str = in.next();
                    initialTemp = Double.parseDouble(str);
                    if (initialTemp <= 0) System.out.println("Ошибка! Введите число больше 0");
                }
                System.out.println("Введите шаг больше нуля");
                if (in.hasNext()) {
                    str = in.next();
                    step = Double.parseDouble(str);
                }
                System.out.println("Введите минимальную температуру больше нуля");
                if (in.hasNext()) {
                    str = in.next();
                    minTemp = Double.parseDouble(str);
                }
                System.out.println("Введите количество итераций больше нуля");
                if (in.hasNext()) {
                    str = in.next();
                    numIter = Integer.parseInt(str);
                }
                int[][] t = annealing(initialTemp, matrix, step, minTemp, numIter);
                System.out.println();
                print(t);
                System.out.println();
                System.out.println(Error(t));

            } else if (flag == 0) {
                String input = "/Users/user/IdeaProjects/annealing/src/com/company/inp/in.txt";
                matrix = read(input);
                int[][] t = annealing(1, matrix, 0.001, 1e-30, 1000000);
                System.out.println();
                print(t);
                System.out.println();
                System.out.println(Error(t));
            } else if (flag == 2) break;
            else System.out.println("Ошибка! Введите число: 1, 0 или 2");
        } while (true);
    }
}
