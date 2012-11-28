package at.meikel.util.excelconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ExcelConverterMain {
    public static final String DELIMITER = ";";
    public static final int SHEET_INDEX = 0;

    private static final int EXIT_STATUS_ARGS_INVALID = 1;
    private final static int EXIT_STATUS_INPUT_FILE_NOT_FOUND = 2;
    private final static int EXIT_STATUS_OUTPUT_FILE_NOT_FOUND = 3;
    private static final int EXIT_STATUS_INPUT_FILE_IO_EXCEPTION = 4;

    private static Logger logger = LoggerFactory.getLogger(ExcelConverter.class);

    public static void main(String[] args) {
        if ((args.length < 2) || (args.length > 4)) {
            logger.error("error.args.invalid");
            printHelp();
            System.exit(EXIT_STATUS_ARGS_INVALID);
        }

        String inputFileName = args[0];
        String outputFileName = args[1];
        String delimiter = DELIMITER;
        int sheetNumber = SHEET_INDEX;
        if (args.length > 2) {
            delimiter = args[2];
        }
        if (args.length > 3) {
            try {
                sheetNumber = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                logger.warn("warn.numberFormatException");
            }
        }

        InputStream inputStream = null;
        PrintStream printStream = null;

        // TODO: check if input file exists, is a "normal" file and can be read
        // TODO: check if output file doesn't exist

        try {
            File inputFile = new File(inputFileName);
            inputStream = getInputStream(inputFile);
        } catch (FileNotFoundException e) {
            logger.error("error.file.notFound", e);
            System.exit(EXIT_STATUS_INPUT_FILE_NOT_FOUND);
        }

        if ("-".equals(outputFileName)) {
            printStream = System.out;
        } else {
            try {
                File outputFile = new File(outputFileName);
                printStream = new PrintStream(getOutputStream(outputFile));
            } catch (FileNotFoundException e) {
                logger.error("error.file.notFound", e);
                System.exit(EXIT_STATUS_OUTPUT_FILE_NOT_FOUND);
            }
        }
        ExcelConverter conv = new ExcelConverter();

        try {
            if (inputFileName.toLowerCase().endsWith(".xls")) {
                conv.processXlsFile(SHEET_INDEX, DELIMITER, inputStream, printStream);
            } else {
                conv.processXlsxFile(SHEET_INDEX, DELIMITER, inputStream, printStream);
            }
        } catch (IOException e) {
            logger.error("error.file.ioException", e);
            System.exit(EXIT_STATUS_INPUT_FILE_IO_EXCEPTION);
        }

        // TODO: close resources
    }

    private static void printHelp() {
        System.err.println("Usage:");
        System.err.println("java -cp at.meikel.util.excelconverter.jar at.meikel.util.excelconverter.ExcelConverterMain <inputFileName> <outputFileName> [ <delimiter> ] [ <sheetNumber> ]");
        System.err.println("<inputFileName> ist the file name of the XLS file to process");
        System.err.println("<outputFileName> ist the file name of the CSV file to be generated");
        System.err.println("<delimiter> is used to separate the fields in the output file (semicolon ; is used as default)");
        System.err.println("<sheetNumber> is the number (starting at 0) of the sheet to be processed (0 is used as default)");
    }

    private static InputStream getInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private static OutputStream getOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

}
