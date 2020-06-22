/*
 * Technical task for Danske bankas
 */
package lt.andrikonis.penkiolika;

/**
 * Convenience methods for test classes.
 *
 * @author julius
 */
public class BaseTest {

    /**
     * Logs the start of the test.
     *
     * @param testName the name of the test.
     */
    protected void logTestStart(String testName) {
        this.logTest("Test " + testName + " started");
    }

    /**
     * Logs the end of the test.
     *
     * @param testName the name of the test.
     */
    protected void logTestEnd(String testName) {
        this.logTest("Test " + testName + " completed");
    }

    private void logTest(String text) {
        BaseTest.log("--------------- " + text + " ---------------");
    }

    /**
     * Logs the start of the tests of specified class.
     *
     * @param clas the clas which is being tested.
     */
    protected static void logClassStart(Class<?> clas) {
        BaseTest.logClass("Test class " + clas.getCanonicalName() + " started");
    }

    /**
     * Logs the end of the tests of specified class.
     *
     * @param clas the clas which is being tested.
     */
    protected static void logClassEnd(Class<?> clas) {
        BaseTest.logClass("Test class " + clas.getCanonicalName() + " completed");
    }

    private static void logClass(String text) {
        BaseTest.log("*************** " + text + " ***************");
    }

    /**
     * Log the test events. <br>
     * TODO: proper logger might be added here.
     *
     * @param text message to log.
     */
    protected static void log(String text) {
        System.out.println(text);
    }
}
