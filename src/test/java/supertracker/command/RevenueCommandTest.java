package supertracker.command;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import supertracker.TrackerException;
import supertracker.item.Inventory;
import supertracker.item.TransactionList;
import supertracker.parser.Parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RevenueCommandTest {
    public static final DateTimeFormatter VALID_USER_INPUT_EX_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String INVALID_EX_DATE_FORMAT = "dd-MM-yyyyy";
    private static final String INVALID_EX_DATE = "01-01-99999";
    private static final DateTimeFormatter VALID_EX_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final LocalDate currDate = LocalDate.now();
    private static final String currDateFormatted = currDate.format(VALID_EX_DATE_FORMAT);
    private static final LocalDate oneDayAheadDate = currDate.plusDays(1);
    private static final String oneDayAheadFormatted = oneDayAheadDate.format(VALID_EX_DATE_FORMAT);
    private static final String oneDayAheadUserInput = oneDayAheadDate.format(VALID_USER_INPUT_EX_DATE_FORMAT);
    private static final LocalDate twoDaysAgoDate = currDate.minusDays(2);
    private static final String twoDaysAgoDateFormatted = twoDaysAgoDate.format(VALID_EX_DATE_FORMAT);
    private static final String twoDaysAgoUserInput = twoDaysAgoDate.format(VALID_USER_INPUT_EX_DATE_FORMAT);
    private static final LocalDate threeDaysAgoDate = currDate.minusDays(3);
    private static final String threeDayAgoFormatted = threeDaysAgoDate.format(VALID_EX_DATE_FORMAT);
    private static final String threeDayAgoUserInput = threeDaysAgoDate.format(VALID_USER_INPUT_EX_DATE_FORMAT);
    private static final LocalDate twoWeeksAgoDate = currDate.minusWeeks(2);
    private static final String twoWeeksAgoDateFormatted = twoWeeksAgoDate.format(VALID_EX_DATE_FORMAT);
    private static final LocalDate invalidDate = LocalDate.parse
            (INVALID_EX_DATE, DateTimeFormatter.ofPattern(INVALID_EX_DATE_FORMAT));
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    /**
     * Sets up the inventory and executes initial commands before running any test.
     * Clears the inventory, then executes a series of commands to populate it with items.
     */
    @BeforeAll
    public static void setUp() {
        Inventory.clear();
        TransactionList.clear();

        Command[] commands = {
            new NewCommand("orange", 20, 2.00, invalidDate),
            new NewCommand("apple", 10, 1.00, invalidDate),
            new NewCommand("banana", 5, 3.00, invalidDate),
            new SellCommand("orange", 20, currDate),
            new SellCommand("apple", 10, twoDaysAgoDate),
            new SellCommand("banana", 5, twoWeeksAgoDate),
        };
        for (Command c : commands) {
            c.execute();
        }
    }

    /**
     * Redirects system output to a PrintStream for capturing output during tests.
     */
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    /**
     * Tests the construction of expenditure report for today's transactions.
     * Verifies that the correct output is printed based on executed commands.
     */
    @Test
    public void revenueCommand_today_correctlyConstructed() throws TrackerException {
        String userInput = "rev type/today";
        Command c = Parser.parseCommand(userInput);
        c.execute();

        Double amount = (double) (20 * 2);
        String amountString = String.format("%.2f", amount);

        String expected = "     Today's revenue is $" + amountString + LINE_SEPARATOR +
                "     1. Name: orange" + LINE_SEPARATOR +
                "        Quantity: 20" + LINE_SEPARATOR +
                "        Price: $2.00" + LINE_SEPARATOR +
                "        Transaction Date: " + currDateFormatted + LINE_SEPARATOR;
        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * Tests the construction of total expenditure report.
     * Verifies that the correct total expenditure report is printed based on executed commands.
     */
    @Test
    public void revenueCommand_total_correctlyConstructed() throws TrackerException {
        String userInput = "rev type/total";
        Command c = Parser.parseCommand(userInput);
        c.execute();

        Double amount = (double) (20 * 2 + 10 * 1 + 5 * 3);
        String amountString = String.format("%.2f", amount);

        String expected = "     Total revenue is $" + amountString + LINE_SEPARATOR +
                "     1. Name: orange" + LINE_SEPARATOR +
                "        Quantity: 20" + LINE_SEPARATOR +
                "        Price: $2.00" + LINE_SEPARATOR +
                "        Transaction Date: " + currDateFormatted + LINE_SEPARATOR +
                "     2. Name: apple" + LINE_SEPARATOR +
                "        Quantity: 10" + LINE_SEPARATOR +
                "        Price: $1.00" + LINE_SEPARATOR +
                "        Transaction Date: " + twoDaysAgoDateFormatted + LINE_SEPARATOR +
                "     3. Name: banana" + LINE_SEPARATOR +
                "        Quantity: 5" + LINE_SEPARATOR +
                "        Price: $3.00" + LINE_SEPARATOR +
                "        Transaction Date: " + twoWeeksAgoDateFormatted + LINE_SEPARATOR;

        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * Tests the construction of expenditure report for a specific day.
     * Verifies that the correct expenditure report for a given day is printed.
     */
    @Test
    public void revenueCommand_day_correctlyConstructed() throws TrackerException {
        String userInput = "rev type/day from/" + twoDaysAgoUserInput;
        Command c = Parser.parseCommand(userInput);
        c.execute();

        Double amount = (double) (10 * 1);
        String amountString = String.format("%.2f", amount);

        String expected = "     revenue on " + twoDaysAgoDateFormatted + " was $" + amountString + LINE_SEPARATOR +
                "     1. Name: apple" + LINE_SEPARATOR +
                "        Quantity: 10" + LINE_SEPARATOR +
                "        Price: $1.00" + LINE_SEPARATOR +
                "        Transaction Date: " + twoDaysAgoDateFormatted + LINE_SEPARATOR;

        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * Tests the construction of expenditure report for a date range.
     * Verifies that the correct expenditure report for a given date range is printed.
     */
    @Test
    public void revenueCommand_range_correctlyConstructed() throws TrackerException {
        String userInput = "rev type/range from/" + threeDayAgoUserInput + " to/" + oneDayAheadUserInput;
        Command c = Parser.parseCommand(userInput);
        c.execute();

        Double amount = (double) (20 * 2 + 10 * 1);
        String amountString = String.format("%.2f", amount);

        String expected = "     revenue between " + threeDayAgoFormatted + " and " + oneDayAheadFormatted
                + " was $" + amountString + LINE_SEPARATOR +
                "     1. Name: orange" + LINE_SEPARATOR +
                "        Quantity: 20" + LINE_SEPARATOR +
                "        Price: $2.00" + LINE_SEPARATOR +
                "        Transaction Date: " + currDateFormatted + LINE_SEPARATOR +
                "     2. Name: apple" + LINE_SEPARATOR +
                "        Quantity: 10" + LINE_SEPARATOR +
                "        Price: $1.00" + LINE_SEPARATOR +
                "        Transaction Date: " + twoDaysAgoDateFormatted + LINE_SEPARATOR;

        String actual = outContent.toString();
        assertEquals(expected, actual);
    }

    /**
     * Tests the behavior when the user input for expenditure command is incomplete.
     * Verifies that a TrackerException is thrown when required parameters are missing.
     */
    @Test
    public void revenueCommand_missingParamInput() {
        String userInput = "exp type/";
        assertThrows(TrackerException.class, () -> Parser.parseCommand(userInput));
    }

    /**
     * Tests the behavior when the user input for expenditure command has too many parameters.
     * Verifies that a TrackerException is thrown when unexpected parameters are provided.
     */
    @Test
    public void revenueCommand_tooManyParamInput() {
        String userInput = "exp type/total from/";
        assertThrows(TrackerException.class, () -> Parser.parseCommand(userInput));
    }

    /**
     * Tests the behavior when the user input for expenditure command is missing the flags for day task.
     * Verifies that a TrackerException is thrown when essential flags are absent.
     */
    @Test
    public void revenueCommand_missingDayFlag() {
        String userInput = "exp type/day";
        assertThrows(TrackerException.class, () -> Parser.parseCommand(userInput));
    }

    /**
     * Tests the behavior when the user input for expenditure command is missing the flag for range task.
     * Verifies that a TrackerException is thrown when the range flag is missing.
     */
    @Test
    public void revenueCommand_missingRangeFlag() {
        String userInput = "exp type/range from/" + threeDayAgoUserInput;
        assertThrows(TrackerException.class, () -> Parser.parseCommand(userInput));
    }
}