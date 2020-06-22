/*
 * Technical task for Danske bankas
 */
package lt.andrikonis.penkiolika;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;

/**
 * Tests for Server and ServerHandler classes.
 *
 * @author julius
 */
public class ServerHandlerTest extends BaseTest {

    /**
     * Port of the test server: {@value #TEST_SERVER_PORT}.
     */
    public static final int TEST_SERVER_PORT = 8081;

    // Test game id
    private static final String INDEX = "0";

    // Mocked game object
    private Penkiolika gameMock;

    // Started server
    private HttpServer server;

    /**
     * Before starting all the tests of this class.
     */
    @BeforeAll
    public static void setUpClass() {
        BaseTest.logClassStart(ServerHandler.class);
        ServerHandlerTest.allowMoreHttpMethods();
    }

    /**
     * After completing all the tests of this class.
     */
    @AfterAll
    public static void tearDownClass() {
        BaseTest.logClassEnd(ServerHandler.class);
    }

    /**
     * Before starting each test. Precreate one mocked game and start the server
     * with this game already started.
     * @throws IOException if input output exception occurs whils starting the server.
     */
    @BeforeEach
    public void setUp() throws IOException {
        HashMap<String, Penkiolika> games = new HashMap<String, Penkiolika>();
        gameMock = Mockito.mock(Penkiolika.class);
        games.put(INDEX, gameMock);
        server = Server.start(TEST_SERVER_PORT, ServerHandler.BASE_PATH, new ServerHandler(games));
    }

    /**
     * After completing each test. Stop the server.
     */
    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    /**
     * Test create new game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testPostNewGame() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testPostNewGame";
        this.logTestStart(testName);
        // Test
        HttpResponse response1 = this.doRequest("POST", ServerHandler.GAME_PATH + "/");
        HttpResponse response2 = this.doRequest("POST", ServerHandler.GAME_PATH + "/");
        // Result validation
        JSONObject json1 = new JSONObject(response1.getBody());
        JSONObject json2 = new JSONObject(response2.getBody());
        String index1 = json1.getString(ServerHandler.JSON_ID);
        String index2 = json2.getString(ServerHandler.JSON_ID);
        int[] board1 = this.getIntArray(json1.getJSONArray(ServerHandler.JSON_BOARD));
        int[] board2 = this.getIntArray(json2.getJSONArray(ServerHandler.JSON_BOARD));
        assertEquals(201, response1.getStatusCode());
        assertEquals(201, response2.getStatusCode());
        assertNotEquals(index1, index2);
        assertFalse(Arrays.equals(board1, board2)); // on the very unlikely turn of events, this might fail
        assertFalse(json1.getBoolean(ServerHandler.JSON_FINAL));
        assertFalse(json2.getBoolean(ServerHandler.JSON_FINAL));
        this.logTestEnd(testName);
    }

    /**
     * Test retrieve game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testGetGame() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testGetGame";
        this.logTestStart(testName);
        // Mocks
        int[] board = new int[]{3,5,7,2,1,10,4,11,15,6,0,13,12,9,8,14};
        Mockito.when(gameMock.getBoard()).thenReturn(board);
        Mockito.when(gameMock.isFinal()).thenReturn(false);
        // Test
        HttpResponse response = this.doRequest("GET", ServerHandler.GAME_PATH + "/" + INDEX);
        // Result validation
        JSONObject json = new JSONObject(response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(INDEX, json.getString(ServerHandler.JSON_ID));
        assertArrayEquals(board, this.getIntArray(json.getJSONArray(ServerHandler.JSON_BOARD)));
        assertFalse(json.getBoolean(ServerHandler.JSON_FINAL));
        this.logTestEnd(testName);
    }

    /**
     * Test make move left to a game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testPatchDoMoveLeft() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testPatchDoMoveLeft";
        this.logTestStart(testName);
        // Additional mock
        Mockito.when(gameMock.moveLeft()).thenReturn(true);
        // General test
        this.testPatchDoMove(ServerHandler.JSON_MOVE_LEFT);
        // Additional validation
        Mockito.verify(gameMock).moveLeft();
        Mockito.verify(gameMock, Mockito.never()).moveRight();
        Mockito.verify(gameMock, Mockito.never()).moveTop();
        Mockito.verify(gameMock, Mockito.never()).moveBottom();
        this.logTestEnd(testName);
    }

    /**
     * Test make move right to a game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testPatchDoMoveRight() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testPatchDoMoveRight";
        this.logTestStart(testName);
        // Additional mock
        Mockito.when(gameMock.moveRight()).thenReturn(true);
        // General test
        this.testPatchDoMove(ServerHandler.JSON_MOVE_RIGHT);
        // Additional validation
        Mockito.verify(gameMock).moveRight();
        Mockito.verify(gameMock, Mockito.never()).moveLeft();
        Mockito.verify(gameMock, Mockito.never()).moveTop();
        Mockito.verify(gameMock, Mockito.never()).moveBottom();
        this.logTestEnd(testName);
    }

    /**
     * Test make move top to a game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testPatchDoMoveTop() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testPatchDoMoveTop";
        this.logTestStart(testName);
        // Additional mock
        Mockito.when(gameMock.moveTop()).thenReturn(true);
        // General test
        this.testPatchDoMove(ServerHandler.JSON_MOVE_TOP);
        // Additional validation
        Mockito.verify(gameMock).moveTop();
        Mockito.verify(gameMock, Mockito.never()).moveLeft();
        Mockito.verify(gameMock, Mockito.never()).moveRight();
        Mockito.verify(gameMock, Mockito.never()).moveBottom();
        this.logTestEnd(testName);
    }

    /**
     * Test make move bottom to a game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testPatchDoMoveBottom() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testPatchDoMoveBottom";
        this.logTestStart(testName);
        // Additional mock
        Mockito.when(gameMock.moveBottom()).thenReturn(true);
        // General test
        this.testPatchDoMove(ServerHandler.JSON_MOVE_BOTTOM);
        // Additional validation
        Mockito.verify(gameMock).moveBottom();
        Mockito.verify(gameMock, Mockito.never()).moveLeft();
        Mockito.verify(gameMock, Mockito.never()).moveRight();
        Mockito.verify(gameMock, Mockito.never()).moveTop();
        this.logTestEnd(testName);
    }

    // Convenience method to test the move.
    // jsonMove - a move, which is being tested.
    private void testPatchDoMove(String jsonMove) throws MalformedURLException, ProtocolException, IOException {
        // Mocks
        int[] board = new int[]{3,5,7,2,1,10,4,11,15,6,0,13,12,9,8,14};
        Mockito.when(gameMock.getBoard()).thenReturn(board);
        Mockito.when(gameMock.isFinal()).thenReturn(false);
        // Test
        JSONObject jsonIn = new JSONObject();
        jsonIn.put(ServerHandler.JSON_MOVE, jsonMove);
        HttpResponse response = this.doRequest("PATCH", ServerHandler.GAME_PATH + "/" + INDEX, jsonIn);
        // Result validation
        JSONObject json = new JSONObject(response.getBody());
        assertEquals(200, response.getStatusCode());
        assertEquals(INDEX, json.getString(ServerHandler.JSON_ID));
        assertArrayEquals(board, this.getIntArray(json.getJSONArray(ServerHandler.JSON_BOARD)));
        assertFalse(json.getBoolean(ServerHandler.JSON_FINAL));
    }

    /**
     * Test delete game request.
     * @throws MalformedURLException
     * @throws ProtocolException
     * @throws IOException
     */
    @Test
    public void testDeleteGame() throws MalformedURLException, ProtocolException, IOException {
        String testName = "testDeleteGame";
        this.logTestStart(testName);
        // Mocks
        int[] board = new int[]{3,5,7,2,1,10,4,11,15,6,0,13,12,9,8,14};
        Mockito.when(gameMock.getBoard()).thenReturn(board);
        Mockito.when(gameMock.isFinal()).thenReturn(false);
        // Test
        HttpResponse response1 = this.doRequest("DELETE", ServerHandler.GAME_PATH + "/" + INDEX);
        HttpResponse response2 = this.doRequest("GET", ServerHandler.GAME_PATH + "/" + INDEX);
        // Result validation
        JSONObject json1 = new JSONObject(response1.getBody());
        assertEquals(200, response1.getStatusCode());
        assertEquals(404, response2.getStatusCode());
        assertEquals(INDEX, json1.getString(ServerHandler.JSON_ID));
        assertArrayEquals(board, this.getIntArray(json1.getJSONArray(ServerHandler.JSON_BOARD)));
        assertFalse(json1.getBoolean(ServerHandler.JSON_FINAL));
        this.logTestEnd(testName);
    }

    // Convenience method to make a request.
    // method - a method of the request.
    // path - full path of the request.
    private HttpResponse doRequest(String method, String path) throws MalformedURLException, ProtocolException, IOException  {
        return this.doRequest(method, path, null);
    }

    // Convenience method to make a request.
    // method - a method of the request.
    // path - full path of the request.
    // jsonIn - contents of the request. If null, then request has no contents.
    private HttpResponse doRequest(String method, String path, JSONObject jsonIn) throws MalformedURLException, ProtocolException, IOException  {
        URL url = new URL("http://localhost:" + TEST_SERVER_PORT + ServerHandler.BASE_PATH + "/" + path);
        HttpURLConnection con = (HttpURLConnection)(url.openConnection());
        con.setRequestMethod(method);
        if (jsonIn != null) {
            con.setDoOutput(true);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()))) {
                bw.write(jsonIn.toString());
                bw.flush();
            }
        }
        int status = con.getResponseCode();
        String response;
        InputStream is;
        if (status >= 300) {
            is = con.getErrorStream();
        } else {
            is = con.getInputStream();
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            response = br.lines().collect(Collectors.joining("\n"));
        }
        return new HttpResponse(status, response);
    }

    // Convenience method to convert JSON array of ints to int array.
    private int[] getIntArray(JSONArray array) {
        int[] result = new int[array.length()];
        for (int i=0; i<result.length; i++) {
            result[i] = array.getInt(i);
        }
        return result;
    }

    // Dirty hack to allow PATCH method in HTTP requests: https://stackoverflow.com/a/46323891
    private static void allowMoreHttpMethods() {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<String>(Arrays.asList(oldMethods));
            methodsSet.add("PATCH");
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null/*static field*/, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}

// An object for packing the HTTP response code and contents.
class HttpResponse {
    private final int statusCode;
    private final String body;

    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public String getBody(){
        return body;
    }
}
