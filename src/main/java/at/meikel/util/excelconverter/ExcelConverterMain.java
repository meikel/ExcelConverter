package at.meikel.util.excelconverter;

import com.Ostermiller.util.CmdLn;
import com.Ostermiller.util.CmdLnOption;
import com.Ostermiller.util.CmdLnResult;
import com.Ostermiller.util.UnknownCmdLnOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class ExcelConverterMain {
    public static final String DELIMITER = ";";
    public static final int SHEET_INDEX = 0;

    private static final int EXIT_STATUS_ARGS_INVALID = 1;
    private final static int EXIT_STATUS_INPUT_FILE_NOT_FOUND = 2;
    private final static int EXIT_STATUS_OUTPUT_FILE_NOT_FOUND = 3;
    private static final int EXIT_STATUS_INPUT_FILE_IO_EXCEPTION = 4;

    private static Logger logger = LoggerFactory.getLogger(ExcelConverter.class);

    private enum EnumOptions {
        HELP(new CmdLnOption("help", 'h')),
        INPUT_FILE(new CmdLnOption("input-file", 'i').setRequiredArgument().setDescription("required: file name of the XLS file to process")),
        OUTPUT_FILE(new CmdLnOption("output-file", 'o').setRequiredArgument().setDescription("required: file name of the CSV file to be generated (hyphen - for stdout)")),
        DELIMITER(new CmdLnOption("delimiter", 'd').setOptionalArgument().setDescription("optional: delimiter (of any length) used to separate the fields in the output file (semicolon ; is used as default)")),
        SHEET_NUMBER(new CmdLnOption("sheet-number", 'n').setOptionalArgument().setDescription("optional: the number (starting at 0) of the sheet to be processed (0 is used as default)")),
        //
        ;

        private CmdLnOption option;

        private EnumOptions(CmdLnOption option) {
            option.setUserObject(this);
            this.option = option;
        }

        private CmdLnOption getCmdLineOption() {
            return option;
        }
    }

    ;

    public static void main(String[] args) {
        CmdLn cmdLn = new CmdLn(args);
        for (EnumOptions option : EnumOptions.values()) {
            cmdLn.addOption(option.getCmdLineOption());
        }

        boolean invalidArgs = false;
        boolean printHelp = false;
        String inputFileName = null;
        String outputFileName = null;
        String delimiter = DELIMITER;
        int sheetNumber = SHEET_INDEX;

        try {
            for (CmdLnResult result : cmdLn.getResults()) {
                switch ((EnumOptions) result.getOption().getUserObject()) {
                    case HELP: {
                        printHelp = true;
                    }
                    break;
                    case INPUT_FILE: {
                        inputFileName = result.getArgument();
                    }
                    break;
                    case OUTPUT_FILE: {
                        outputFileName = result.getArgument();
                    }
                    break;
                    case DELIMITER: {
                        if (result.getArgumentCount() > 0) {
                            delimiter = result.getArgument();
                        }
                    }
                    case SHEET_NUMBER: {
                        if (result.getArgumentCount() > 0) {
                            try {
                                sheetNumber = Integer.parseInt(result.getArgument());
                            } catch (NumberFormatException e) {
                                logger.warn("warn.numberFormatException");
                            }
                        }
                    }
                    break;
                }
            }
        } catch (UnknownCmdLnOptionException e) {
            logger.error("Unknown command line option: " + e.getOption());
            invalidArgs = true;
        }

        List<String> nonOptionArguments = cmdLn.getNonOptionArguments();
        if ((nonOptionArguments != null) && (!nonOptionArguments.isEmpty())) {
            StringBuilder msg = new StringBuilder("The following arguments are ignored: ");
            for (String argument : cmdLn.getNonOptionArguments()) {
                msg.append(argument);
                msg.append(" ");
            }
            logger.warn(msg.toString());
            invalidArgs = true;
        }

        if ((inputFileName == null) || (outputFileName == null)) {
            invalidArgs = true;
        }

        if ((printHelp) || (invalidArgs)) {
            System.err.println("Usage:");
            System.err.println("java -jar ..\\..\\target\\ExcelConverter-jar-with-dependencies.jar <options>");
            System.err.println("Valid <options> are:");
            cmdLn.printHelp(System.err);
            System.exit(invalidArgs ? EXIT_STATUS_ARGS_INVALID : 0);
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

    private static InputStream getInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    private static OutputStream getOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
    }

}
