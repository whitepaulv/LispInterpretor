import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class fileChecker {
    static Boolean checkFiles(File test, File expected, Boolean print) throws IOException {
        String fOneText = Files.readString(test.toPath());
        String fTwoText = Files.readString(expected.toPath());

        if (print) {
            System.out.print("Test output:\n\n" + fOneText + "\n\nExpected Output:\n\n" + fTwoText + "\n\n");
        }
        return (fOneText.equals(fTwoText));
    }

    public static void main(String[] args) throws IOException {
        // Put number here:
        Integer testNumber = 2;  // FIXME: EDIT HERE
                                // BEFORE RUNNING THIS FILE, RUN java Main < test/test(x).txt > testOutput/testOutput(x).txt
    
        String fOneName = "testOutput/testOutput" + testNumber.toString() + ".txt";
        String fTwoName = "testResults/testResults" + testNumber.toString() + ".txt";
    
        File fOne = new File(fOneName);
        File fTwo = new File(fTwoName);
        System.out.println(checkFiles(fOne, fTwo, true));
    }
}

