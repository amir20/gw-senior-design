
import java.util.Vector;

/**
 * Recognizes the best matched item compared to a WAVFeature
 *
 * @author Amir Raminfar
 */
public class Recognize {
    private Vector<WAVFeature> library;
    private int[] sample;

    /**
     * Compares feat with the default library
     *
     * @param feat WAVFeature
     */
    public Recognize(WAVFeature feat) {
        this(null, feat);
    }

    /**
     * Compares feat with given library file
     *
     * @param libraryFile
     * @param feat
     */
    public Recognize(Library libraryFile, WAVFeature feat) {
        if (libraryFile == null) {
            library = new Library().getLibrary();
            sample = feat.getFeatures();
        } else {
            library = libraryFile.getLibrary();
            sample = feat.getFeatures();
        }
    }

    /**
     * Creates a matrix given two arrays of features
     *
     * @param A int[]
     * @param B int[]
     */
    private int[][] getMatrix(int[] A, int[] B) {

        // Initialize matrix
        int[][] matrix = new int[A.length][A.length];


        //Compute matrix
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A.length; j++) {
                matrix[i][j] = Math.abs(A[i] - B[j]);
            }
        }
        return matrix;
    }

    /**
     * Find the length of the shortest path in the given matrix
     *
     * @param matrix
     */
    private int getShortestPath(int[][] matrix) {
        // Computer the first row and column
        for (int i = 1; i < matrix.length; i++) {
            matrix[i][0] += matrix[i - 1][0];
            matrix[0][i] += matrix[0][i - 1];
        }
        //Step through the matrix and find shortest path
        for (int i = 1; i < matrix.length; i++) {
            //horizontally
            for (int j = i; j < matrix.length; j++) {
                int min = Math.min(matrix[i][j - 1], Math.min(matrix[i - 1][j], matrix[i - 1][j - 1]));
                matrix[i][j] += min;
            }
            //vertically
            if (i + 1 < matrix.length) {
                for (int j = i + 1; j < matrix.length; j++) {
                    int min = Math.min(matrix[j][i - 1], Math.min(matrix[j - 1][i], matrix[j - 1][i - 1]));
                    matrix[j][i] += min;
                }
            }
        }
        //return the length
        return matrix[matrix.length - 1][matrix.length - 1];
    }

    /**
     * Finds the best suitable application and returns an instance of it
     */
    public WAVFeature getApplication() {
        int min = Integer.MAX_VALUE;
        WAVFeature minApp = null;
        for (int i = 0; i < library.size(); i++) {
            //get one item
            WAVFeature temp = library.get(i);
            //compute items path
            int path = getShortestPath(getMatrix(temp.getFeatures(), sample));
            //check if it is less then current min
            if (path < min) {
                min = path;
                minApp = temp;
            }
        }
        //Update library statistics
        Library lib = new Library();
        if (minApp != null && lib.getLibrary().indexOf(minApp) != -1) {
            int index = lib.getLibrary().indexOf(minApp);
            minApp.setTotal(minApp.getTotal() + 1);
            lib.getLibrary().setElementAt(minApp, index);
            lib.saveLibraryToDisk();
        }
        return minApp;
    }

    /**
     * Gets length of a sample
     *
     * @param wav
     */
    public int getSampleLength(int[] wav) {
        int fsize = 0;
        //find first non-zero element
        while (fsize < wav.length) {
            if (wav[fsize] != 0)
                break;
            fsize++;
        }
        int lsize = wav.length - 1;
        //find last non-zero element
        while (lsize > 0) {
            if (wav[lsize] != 0)
                break;
            lsize--;
        }
        return lsize - fsize;
    }

    /**
     * Used for recognizing the word 'computer'. This function returns a value which represents the difference between the
     * sample and the word computer. The smaller the number the better the match.
     */
    public int getShortestPath() {
        WAVFeature temp = library.firstElement();
        int sum = 0;
        for (int b : sample) sum += b;
        if (sum < 350 || Math.abs((getSampleLength(temp.getFeatures()) - getSampleLength(sample))) > 18) return Integer.MAX_VALUE;
        int path = getShortestPath(getMatrix(temp.getFeatures(), sample));
        return path;
    }
}
