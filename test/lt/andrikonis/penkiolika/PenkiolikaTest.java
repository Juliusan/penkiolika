/*
 * Technical task for Danske bankas
 */
package lt.andrikonis.penkiolika;

import java.util.Arrays;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Penkiolika class.
 *
 * @author julius
 */
public class PenkiolikaTest extends BaseTest {
    private Penkiolika p00;
    private Penkiolika p02;
    private Penkiolika p10;
    private Penkiolika p22;
    private Penkiolika p23;
    private Penkiolika p31;
    private Penkiolika p33;

    /**
     * Before starting all the tests of this class.
     */
    @BeforeAll
    public static void setUpClass() {
        BaseTest.logClassStart(Penkiolika.class);
    }

    /**
     * After completing all the tests of this class.
     */
    @AfterAll
    public static void tearDownClass() {
        BaseTest.logClassEnd(Penkiolika.class);
    }

    /**
     * Before starting each test. Reinitialise the list of tested games.
     */
    @BeforeEach
    public void setUp() {
        p00 = new Penkiolika(new int[]{0,5,7,2,1,10,4,11,15,6,3,13,12,9,8,14});
        p02 = new Penkiolika(new int[]{3,5,0,2,1,10,4,11,15,6,7,13,12,9,8,14});
        p10 = new Penkiolika(new int[]{3,5,7,2,0,10,4,11,15,6,1,13,12,9,8,14});
        p22 = new Penkiolika(new int[]{3,5,7,2,1,10,4,11,15,6,0,13,12,9,8,14});
        p23 = new Penkiolika(new int[]{3,5,7,2,1,10,4,11,15,6,13,0,12,9,8,14});
        p31 = new Penkiolika(new int[]{3,5,7,2,1,10,4,11,15,6,9,13,12,0,8,14});
        p33 = new Penkiolika(new int[]{3,5,7,2,1,10,4,11,15,6,14,13,12,9,8,0});
    }

    /**
     * After completing each test.
     */
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of shuffle method, of class Penkiolika.
     */
    @Test
    public void testShuffle() {
        String testName = "shuffle/0 and shuffle/1";
        this.logTestStart(testName);
        Penkiolika p1 = new Penkiolika();
        Penkiolika p2 = new Penkiolika();
        p1.shuffle();
        p2.shuffle(51);
        assertFalse(p1.equals(p2)); // on the very unlikely turn of events, this might fail
        assertFalse(Arrays.equals(p1.getBoard(), Penkiolika.FINAL_BOARD));
        assertFalse(Arrays.equals(p2.getBoard(), Penkiolika.FINAL_BOARD));
        this.logTestEnd(testName);
    }

    /**
     * Test of isFinal method, of class Penkiolika.
     */
    @Test
    public void testIsFinal() {
        String testName = "isFinal/0";
        this.logTestStart(testName);
        Penkiolika p = new Penkiolika(Penkiolika.FINAL_BOARD);
        assertTrue(p.isFinal());
        assertFalse(p00.isFinal());
        assertFalse(p02.isFinal());
        assertFalse(p10.isFinal());
        assertFalse(p22.isFinal());
        assertFalse(p23.isFinal());
        assertFalse(p31.isFinal());
        assertFalse(p33.isFinal());
        this.logTestEnd(testName);
    }

    /**
     * Test of moveTop method, of class Penkiolika.
     */
    @Test
    public void testMoveTop() {
        String testName = "moveTop/0";
        this.logTestStart(testName);
        Function<Penkiolika, Boolean> moveTop = p -> p.moveTop();
        assertCannotMove(p00, moveTop);
        assertCannotMove(p02, moveTop);
        assertMoved(     p10, moveTop, new int[]{0,5,7,2,3,10,4,11,15,6,1,13,12,9,8,14});
        assertMoved(     p22, moveTop, new int[]{3,5,7,2,1,10,0,11,15,6,4,13,12,9,8,14});
        assertMoved(     p23, moveTop, new int[]{3,5,7,2,1,10,4,0,15,6,13,11,12,9,8,14});
        assertMoved(     p31, moveTop, new int[]{3,5,7,2,1,10,4,11,15,0,9,13,12,6,8,14});
        assertMoved(     p33, moveTop, new int[]{3,5,7,2,1,10,4,11,15,6,14,0,12,9,8,13});
        this.logTestEnd(testName);
    }

    /**
     * Test of moveBottom method, of class Penkiolika.
     */
    @Test
    public void testMoveBottom() {
        String testName = "moveBottom/0";
        this.logTestStart(testName);
        Function<Penkiolika, Boolean> moveBottom = p -> p.moveBottom();
        assertMoved(     p00, moveBottom, new int[]{1,5,7,2,0,10,4,11,15,6,3,13,12,9,8,14});
        assertMoved(     p02, moveBottom, new int[]{3,5,4,2,1,10,0,11,15,6,7,13,12,9,8,14});
        assertMoved(     p10, moveBottom, new int[]{3,5,7,2,15,10,4,11,0,6,1,13,12,9,8,14});
        assertMoved(     p22, moveBottom, new int[]{3,5,7,2,1,10,4,11,15,6,8,13,12,9,0,14});
        assertMoved(     p23, moveBottom, new int[]{3,5,7,2,1,10,4,11,15,6,13,14,12,9,8,0});
        assertCannotMove(p31, moveBottom);
        assertCannotMove(p33, moveBottom);
        this.logTestEnd(testName);
    }

    /**
     * Test of moveLeft method, of class Penkiolika.
     */
    @Test
    public void testMoveLeft() {
        String testName = "moveLeft/0";
        this.logTestStart(testName);
        Function<Penkiolika, Boolean> moveLeft = p -> p.moveLeft();
        assertCannotMove(p00, moveLeft);
        assertMoved(     p02, moveLeft, new int[]{3,0,5,2,1,10,4,11,15,6,7,13,12,9,8,14});
        assertCannotMove(p10, moveLeft);
        assertMoved(     p22, moveLeft, new int[]{3,5,7,2,1,10,4,11,15,0,6,13,12,9,8,14});
        assertMoved(     p23, moveLeft, new int[]{3,5,7,2,1,10,4,11,15,6,0,13,12,9,8,14});
        assertMoved(     p31, moveLeft, new int[]{3,5,7,2,1,10,4,11,15,6,9,13,0,12,8,14});
        assertMoved(     p33, moveLeft, new int[]{3,5,7,2,1,10,4,11,15,6,14,13,12,9,0,8});
        this.logTestEnd(testName);
    }

    /**
     * Test of moveRight method, of class Penkiolika.
     */
    @Test
    public void testMoveRight() {
        String testName = "moveRight/0";
        this.logTestStart(testName);
        Function<Penkiolika, Boolean> moveRight = p -> p.moveRight();
        assertMoved(     p00, moveRight, new int[]{5,0,7,2,1,10,4,11,15,6,3,13,12,9,8,14});
        assertMoved(     p02, moveRight, new int[]{3,5,2,0,1,10,4,11,15,6,7,13,12,9,8,14});
        assertMoved(     p10, moveRight, new int[]{3,5,7,2,10,0,4,11,15,6,1,13,12,9,8,14});
        assertMoved(     p22, moveRight, new int[]{3,5,7,2,1,10,4,11,15,6,13,0,12,9,8,14});
        assertCannotMove(p23, moveRight);
        assertMoved(     p31, moveRight, new int[]{3,5,7,2,1,10,4,11,15,6,9,13,12,8,0,14});
        assertCannotMove(p33, moveRight);
        this.logTestEnd(testName);
    }

    // Convenience method to ensure that the move was performed correctly.
    private static void assertMoved(Penkiolika p, Function<Penkiolika, Boolean> move, int[] finalBoard) {
        assertTrue(move.apply(p));
        assertTrue(Arrays.equals(p.getBoard(), finalBoard));
    }

    // Convenience method to ensure that the move was illegal.
    private static void assertCannotMove(Penkiolika p, Function<Penkiolika, Boolean> move) {
        int[] boardBackup = p.getBoard().clone();
        assertFalse(move.apply(p));
        assertTrue(Arrays.equals(boardBackup, p.getBoard()));
    }
}
