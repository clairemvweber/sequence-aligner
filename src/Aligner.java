import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.StringBuilder;

public class Aligner {
    
    enum Operation {
        MATCH,
        MISMATCH,
        INSERT,
        DELETE
    }

    // Class for holding each Record
    static class Record{
        String problem, x, y;
        StringBuilder cigar;
        int score, yStart, yEnd;
    }

    public static void main(String[] args) {
        String input = args[0];
        String method = args[1];
        int m = -Integer.parseInt(args[2]);
        int g = -Integer.parseInt(args[3]);
        String output = args[4];

        ArrayList<Record> records = new ArrayList<>();

        // Read in ProblemRecord
        try {
            File problemRecord = new File(input);
            Scanner sc = new Scanner(problemRecord);

            Record record = new Record();
            int counter = 0;
            
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                if (counter % 3 == 0) {
                    record.problem = line;
                } else if (counter % 3 == 1) {
                    record.x = line;
                } else {
                    record.y = line;
                    records.add(record);
                    record = new Record();
                }

                counter++;
            }

            sc.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }

        for (Record r: records) {
            int[][] scores;
            Operation[][] paths;

            // Create matrices to hold scores and pointers
            scores = new int[r.x.length() + 1][r.y.length() + 1];
            paths = new Operation[r.x.length() + 1][r.y.length() + 1];

            // Base case along i:
            for (int i = 0; i < r.x.length() + 1; i++) {
                scores[i][0] = i * g;
                paths[i][0] = Operation.INSERT;
            }

            // Base cases along j
            if (method.equals("global")) {
                for (int j = 1; j < r.y.length() + 1; j++) {
                    scores[0][j] = j * g;
                    paths[0][j] = Operation.DELETE;
                }
            } else {
                for (int j = 1; j < r.y.length() + 1; j++) {
                    scores[0][j] = 0;
                    paths[0][j] = Operation.DELETE;
                }
            }

            // Populate matrices
            for (int i = 1; i < r.x.length() + 1; i++) {
                for (int j = 1; j < r.y.length() + 1; j++) {
                    int diag, up, left, max;
                    boolean mismatch = false;

                    // Calculate the potential scores
                    diag = scores[i - 1][j - 1];

                    if (r.x.charAt(i - 1) != r.y.charAt(j - 1)) {
                        mismatch = true;
                        diag += m;
                    }

                    up = scores[i - 1][j] + g;
                    left = scores[i][j - 1] + g;

                    // Find the maximum of the potential scores
                    max = Math.max(diag, Math.max(up, left));

                    // Set matrices according to max
                    scores[i][j] = max;

                    if (max == diag) {
                        if (mismatch) {
                            paths[i][j] = Operation.MISMATCH;
                        } else {
                            paths[i][j] = Operation.MATCH;
                        }
                    } else if (max == up) {
                        paths[i][j] = Operation.INSERT;
                    } else {
                        paths[i][j] = Operation.DELETE;
                    }
                }
            }

            // Determine where on y the alignment ends
            r.yEnd = r.y.length();

            // For fitting method:
            if (method.equals("fitting")) {
                int max = Integer.MIN_VALUE;

                for (int j = 0; j < r.y.length() + 1; j++) {
                    if (max < scores[r.x.length()][j]) {
                        max = scores[r.x.length()][j];
                        r.yEnd = j;
                    }
                }
            }

            // Score equals scores[r.x.length()][r.y.End]
            // yEnd is y.length() for global
            r.score = scores[r.x.length()][r.yEnd];
            
            // Traceback pointers and add to CIGAR string
            r.cigar = new StringBuilder();
            int i = r.x.length(), j = r.yEnd, counter = 1;
            Operation curr = paths[i][j], next;

            while (i > 0 && j > 0) {
                char d = '*';

                if (curr == Operation.INSERT) {
                    d = 'I';
                    i--;
                } else if (curr == Operation.DELETE) {
                    d = 'D';
                    j--;
                } else {
                    if (curr == Operation.MATCH) {
                        d = '=';
                    } else {
                        d = 'X';
                    }

                    i--;
                    j--;
                }

                next = paths[i][j];

                if (curr != next
                    || i == 0 || j == 0
                    || (method.equals("fitting") && i == 0)) {
                    r.cigar.insert(0, d);
                    r.cigar.insert(0, counter);
                    counter = 0;
                }

                // If fitting method, then stop at i == 0 to allow gaps before x
                if (method.equals("fitting") && i == 0) {
                    break;
                }

                curr = next;
                counter++;
            }
            
            // Determine yStart and account for gaps when one string is longer
            // than the other
            if (method.equals("global")) {
                r.yStart = 0;

                if (i > 0) {
                    r.cigar.insert(0, "I");
                    r.cigar.insert(0, i);
                } else if (j > 0) {
                    r.cigar.insert(0, "D");
                    r.cigar.insert(0, j);
                }

            } else {
                r.yStart = j;

                if (i > 0) {
                    r.cigar.insert(0, "I");
                    r.cigar.insert(0, i);
                }
            }
        }
    
        // Write results to output
        try {
            FileWriter fw = new FileWriter(output);
            
            for (Record r : records) {
                StringBuilder sb = new StringBuilder();

                sb.append(r.problem + "\n" + r.x + "\n" + r.y + "\n");
                sb.append(r.score + "\t" + r.yStart + "\t" + r.yEnd + "\t" + r.cigar + "\n");

                fw.write(sb.toString());
            }

            fw.close();

        } catch (IOException e) {
            System.out.println("Error writing file");
        }
    }
}