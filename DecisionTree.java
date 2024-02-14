import java.util.*;
import java.io.*;

class DecisionTree {
    static String[][] data = new String[200][200];
    static double[] gain = new double[200];
    static String[] diffClasses;
    static boolean[] finished;
    static boolean[] visitedAttributes;
    static int columns;
    static int rows;
    public static boolean[] attributesVisited;
    public static String[] classValue;

    public static void inputReader(Scanner scan) {
        String file, line;
        BufferedReader name = null;
        int rows2 = 0;
        System.out.println("Insira o nome do ficheiro que pretende ler. Exemplo: restaurant");
        file = scan.next();
        file += ".csv";
        try {
            name = new BufferedReader(new FileReader(file));
            line = name.readLine();
            while (line != null) {
                String[] words = line.split(",");
                for (int i = 0; i < words.length; i++)
                    data[rows2][i] = words[i];
                rows2++;
                columns = words.length;
                line = name.readLine();
            }
            rows = rows2;
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if (name != null) {
                try {
                    name.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String[] lookValues(int column, boolean[] visited) {
        int k = 0;
        int count;
        String[] used = new String[rows];
        String aux;

        for (int i = 0; i < rows; i++)
            used[i] = "";

        for (int i = 1; i < rows; i++) {
            count = 0;
            for (int j = 0; j <= k; j++) {
                if (visited[i] &&  (!data[i][column].equals(used[j]))) {
                        count++;
                    
                }
            }
            if (count == k + 1) {
                used[k] = data[i][column];
                k++;
            }
        }

        aux = used[0];
        for (int i = 1; i < k; i++) {
            aux = aux + "," + used[i];
        }

        return aux.split(",");

    }

    public static double calcEntropyOrig(boolean[] visited) {
        double positive;
        double negative;
        double original;
        double count = 0;
        double total = 0;

        for (int i = 1; i < rows; i++) {
            if (visited[i]) {
                total++;
            }
        }

        for (int i = 1; i < rows; i++) {
            if (visited[i] &&  (data[i][columns - 1].equals("Yes"))) {
                    count++;
                
            }
        }
        positive = count / total;

        count = 0;
        for (int i = 1; i < rows; i++) {
            if (visited[i] &&  (data[i][columns - 1].equals("No"))) {
                    count++;
                
            }
        }
        negative = count / total;

        original = (positive * (Math.log(positive) / Math.log(2))) + (negative * (Math.log(negative) / Math.log(2)));
        if (Double.isNaN(original))
            return 0;
        return -original;
    }

    public static double calcProb(int column, String value, boolean[] visited) {
        int numerador = 0;
        int total = 0;

        for (int i = 1; i < rows; i++) {
            if (visited[i])
                total++;
        }

        for (int i = 1; i < rows; i++) {
            if (visited[i] &&  (data[i][column].equals(value))) {
                    numerador++;
                
            }
        }

        return (double) numerador / total;

    }

    public static double calcProbParcial(int column, String value, int comp, boolean[] visited) {
        int count = 0;
        int positive = 0;
        for (int i = 1; i < rows; i++) {
            if (visited[i] &&  (data[i][column].equals(value))) {
                    count++;
                    if (data[i][columns - 1].equals(classValue[comp]))
                        positive++;
                
            }
        }

        return (double) positive / (double) count;
    }

    public static int calcCounter(int column, String value, boolean[] visited) {
        int counter = 0;
        for (int i = 1; i < rows; i++) {
            if (visited[i] &&  (data[i][column].equals(value))) {
                    counter++;
                
            }
        }

        return counter;
    }

    public static double calcEntropy(double positive, double negative) {
        double entropy = (positive * (Math.log(positive) / Math.log(2)))
                + (negative * (Math.log(negative) / Math.log(2)));

        if (Double.isNaN(entropy))
            return 0;

        return -entropy;

    }

    public static void calcGain(boolean[] visited) {
        String[] values;
        double probability;
        double positive;
        double negative;
        double entropy;
        double entropyOrig;

        for (int i = 0; i < columns; i++) {
            gain[i] = 0;
        }

        entropyOrig = calcEntropyOrig(visited);

        for (int i = 1; i < columns - 1; i++) {
            values = lookValues(i, visited);

            for (int j = 0; j < values.length; j++) {
                probability = calcProb(i, values[j], visited);
                positive = calcProbParcial(i, values[j], 1, visited);
                negative = calcProbParcial(i, values[j], 0, visited);
                entropy = calcEntropy(positive, negative);
                gain[i] += probability * entropy;
            }
        }

        for (int i = 1; i < columns - 1; i++) {
            gain[i] = entropyOrig - gain[i];
        }

    }

    public static void printSpace(int space) {
        for (int i = 0; i < space; i++)
            System.out.print(" ");
    }

    public static boolean[] refreshvisited(int column, String value, boolean[] visited) {
        for (int i = 1; i < rows; i++) {
            if (data[i][column].equals(value))
                visited[i] = false;
        }

        return visited;
    }

    public static LinkedList<String> ID3(LinkedList<String> tree, String[] examples, int targetAttribute,
            String[][] attributes, boolean[] visited, int space) {
        int countProb;
        int counter;

        tree.addLast(attributes[0][targetAttribute]);
        attributesVisited[targetAttribute] = true;
        printSpace(space);
        System.out.println("<" + attributes[0][targetAttribute] + ">"); // Imprimir atributo

        for (int i = 0; i < classValue.length; i++) {
            countProb = 0;
            for (int j = 0; j < examples.length; j++) {
                if (calcProbParcial(targetAttribute, examples[j], i, visited) == 1) {
                    countProb++;
                }
            }
            if (countProb == examples.length) {
                for (int k = 0; k < countProb; k++) {
                    printSpace(space + 4);
                    System.out.println(examples[k] + ": " + classValue[i] + " (" + countProb + ")");
                }
                return tree;
            }
        }

        boolean[] examplesVisited = new boolean[examples.length]; // Marcar todos os exemplos como nao lidos
        for (int k = 0; k < examples.length; k++) {
            examplesVisited[k] = false;
        }

        for (int i = 0; i < classValue.length; i++) {
            for (int j = 0; j < examples.length; j++) {
                if (!examplesVisited[j] &&  (calcProbParcial(targetAttribute, examples[j], i, visited) == 1)) {
                        counter = calcCounter(targetAttribute, examples[j], visited);
                        tree.addLast(classValue[i]);
                        printSpace(space + 4);
                        System.out.println(examples[j] + ": " + classValue[i] + " (" + counter + ")");
                        examplesVisited[j] = true;
                        visited = refreshvisited(targetAttribute, examples[j], visited);
                    
                }
            }
        }

        for (int i = 0; i < examples.length; i++) {
            if (!examplesVisited[i]) {
                double bestGain = -1;
                int posRoot = -1;

                calcGain(visited);

                for (int j = 1; j < columns - 1; j++) {
                    if (!attributesVisited[j] && gain[j] > bestGain) {
                        bestGain = gain[j];
                        posRoot = j;
                    }
                }
                if (bestGain == -1)
                    return tree;
                printSpace(space + 4);
                System.out.println(examples[i] + ": ");
                ID3(tree, lookValues(posRoot, visited), posRoot, attributes, visited, space + 8);

            }
        }

        return tree;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int posRoot = -1;
        boolean[] visited;
        double bestGain = -1;

        inputReader(in);
        System.out.println();
        visited = new boolean[rows];
        for (int i = 0; i < rows; i++)
            visited[i] = true;

        classValue = lookValues(columns - 1, visited);

        calcGain(visited);

        for (int i = 1; i < columns - 1; i++) {
            if (gain[i] > bestGain) {
                bestGain = gain[i];
                posRoot = i;
            }
        }

        attributesVisited = new boolean[columns];
        for (int i = 0; i < columns; i++) {
            attributesVisited[i] = false;
        }

        LinkedList<String> tree = new LinkedList<String>();
        ID3(tree, lookValues(posRoot, visited), posRoot, data, visited, 0);

        System.out.println();

    }
}
