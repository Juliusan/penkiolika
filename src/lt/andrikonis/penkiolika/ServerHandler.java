/*
 * Technical task for Danske bankas
 */
package lt.andrikonis.penkiolika;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.function.Function;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Fifteen ("Penkiolika") game request handler. For a game description see
 * <a href="https://en.wikipedia.org/wiki/15_puzzle">https://en.wikipedia.org/wiki/15_puzzle</a>.
 * This handler listens to the following requests:
 * <ul>
 *  <li>{@code POST} request to {@code /penkiolika/game/} - creates a new game
 *      and returns it. Returns HTTP status 201 on success.
 *  <li>{@code GET} request to {@code /penkiolika/game/<id>} - returns an existing
 *      game. Returns HTTP status 200 on success and 404 if the game is not found.
 *  <li>{@code PATCH} request to {@code /penkiolika/game/<id>} - orders a server
 *      to make a move in the specified game. A move is specified in JSON using
 *      this syntax: {@code {"move":"left"|"right"|"top"|"bottom"}}. Returns the
 *      game after the move. Returns HTTP status 200 on success, 404 if the game
 *      is not found and 409 if the move is not legal in current game state.
 *  <li>{@code DELETE} request to {@code /penkiolika/game/<id>} - deletes the
 *      specified game and returns the deleted game.
 * </ul>
 *
 * The {@code <id>} parameter in theese requests is an id of the game, which is
 * returned every time a game is returned from the request, including on game's
 * creation.
 * <p>
 * A returned game follows the following JSON format:
 * <pre>
 * {@code
 *  {
 *      "id":<id>,
 *      "board":<game state>,
 *      "final":true|false
 *  }
 * }
 * </pre>
 *
 * The {@code <id>} is a string value. <br>
 * The {@code <game state>} is an array of integers. It contains exactly 16 elements
 * and the empty space is noted by the value 0. If the elements of the array are
 * indexed starting 0, then the game board should be filled in the following order:
 * <pre>
 * {@code
 *   state[ 0] state[ 1] state[ 2] state[ 3]
 *   state[ 4] state[ 5] state[ 6] state[ 7]
 *   state[ 8] state[ 9] state[10] state[11]
 *   state[12] state[13] state[14] state[15]
 * }
 * </pre>
 * The {@code "final"} boolean parameter is true if and only if the game state is
 * final. This means that the numbers in the bord are in consecutive order and that
 * the empty space is in the bottom right corner of the board.<br>
 * <p>
 * The errors are returned as JSON objects {@code {"reason":<reason>}}, where
 * {@code <reason>} is a string with error details.
 *
 * @author julius
 */
public class ServerHandler implements HttpHandler {

    /**
     * The base path of the requests for this handler: {@value #BASE_PATH}.
     */
    public static final String BASE_PATH = "/penkiolika";

    /**
     * The subpath for requests about games for this handler: {@value #GAME_PATH}.
     */
    public static final String GAME_PATH = "game";

    /**
     * The id field name of the returned game JSON: {@value #JSON_ID}.
     */
    public static final String JSON_ID = "id";

    /**
     * The board field name of the returned game JSON: {@value #JSON_BOARD}.
     */
    public static final String JSON_BOARD = "board";

    /**
     * The final field name of the returned game JSON: {@value #JSON_FINAL}.
     */
    public static final String JSON_FINAL = "final";

    /**
     * The move field name of the JSON passed as contents to the move request:
     * {@value #JSON_MOVE}.
     */
    public static final String JSON_MOVE = "move";

    /**
     * The "move the empty space to the left" move field value {@value #JSON_MOVE_LEFT}.
     * It is one of the four possible values of {@value #JSON_MOVE} JSON field,
     * which is passed as contents to the move request.
     */
    public static final String JSON_MOVE_LEFT = "left";

    /**
     * The "move the empty space to the right" move field value {@value #JSON_MOVE_RIGHT}.
     * It is one of the four possible values of {@value #JSON_MOVE} JSON field,
     * which is passed as contents to the move request.
     */
    public static final String JSON_MOVE_RIGHT = "right";

    /**
     * The "move the empty space to the top" move field value {@value #JSON_MOVE_TOP}.
     * It is one of the four possible values of {@value #JSON_MOVE} JSON field,
     * which is passed as contents to the move request.
     */
    public static final String JSON_MOVE_TOP = "top";

    /**
     * The "move the empty space to the bottom" move field value {@value #JSON_MOVE_BOTTOM}.
     * It is one of the four possible values of {@value #JSON_MOVE} JSON field,
     * which is passed as contents to the move request.
     */
    public static final String JSON_MOVE_BOTTOM = "bottom";

    /**
     * The error reason field name of the error JSON response: {@value #JSON_ERROR_REASON}.
     */
    public static final String JSON_ERROR_REASON = "reason";

    // All the games, which were created using this handler.
    private final ConcurrentMap<String, Penkiolika> games;
    // The ID of the last game created (or 0, if none have been created yet).
    // It is used to generate unique ids for new games.
    private final AtomicInteger lastId = new AtomicInteger(0);

    /**
     * Creates a Fifteen game request handler with no precreated games.
     */
    public ServerHandler() {
        games = new ConcurrentHashMap<String, Penkiolika>();
    }

    /**
     * Creates a Fifteen game request handler with some games already started.
     *
     * @param games a map of ids to created games.
     */
    public ServerHandler(Map<String, Penkiolika> games) {
        this();
        this.games.putAll(games);
    }

    /**
     * Handle the given request to this handler and generate an appropriate
     * response.
     *
     * @param he the exchange containing the request from the client and used to
     * send the response.
     *
     * @throws IOException if input output exception occurs while reading the request
     * or sending the response.
     */
    @Override
    public void handle(HttpExchange he) throws IOException {
        String method = he.getRequestMethod();
        String fullPath = he.getRequestURI().getPath();
        if (fullPath.startsWith(BASE_PATH)) {
            String relativePath = fullPath.substring(BASE_PATH.length());
            String[] relativePathElems = relativePath.split("/");
            if (method.equals("POST") && this.pathMatches(relativePathElems, new String[]{GAME_PATH})) {
                handlePostNewGame(he);
            } else if (method.equals("GET") && this.pathMatches(relativePathElems, new String[]{GAME_PATH, null})) {
                handleGetGame(he, relativePathElems[2]);
            } else if (method.equals("PATCH") && this.pathMatches(relativePathElems, new String[]{GAME_PATH, null})) {
                String body;
                try(BufferedReader br = new BufferedReader(new InputStreamReader(he.getRequestBody()))) {
                    body = br.lines().collect(Collectors.joining("\n"));
                }
                try{
                    JSONObject json = new JSONObject(body);
                    handlePatchDoMove(he, relativePathElems[2], json);
                } catch (JSONException jsone) {
                    this.respondError(he, 415, "JSON object contents is expected, received: " + body + ". " + jsone.getMessage());
                }
            } else if (method.equals("DELETE") && this.pathMatches(relativePathElems, new String[]{GAME_PATH, null})) {
                handleDeleteGame(he, relativePathElems[2]);
            } else {
                this.respondError(he, 400, "Method " + method + " for path is not supported: " + fullPath);
            }
        } else {
            this.respondError(he, 500, "Wrong path for this handler: " + fullPath);
        }

    }

    // Handles the POST request to create a new game.
    private void handlePostNewGame(HttpExchange he) throws IOException {
        String id = "" + lastId.incrementAndGet();
        Penkiolika game = new Penkiolika();
        game.shuffle();
        games.put(id, game);
        JSONObject json = this.getGameJson(id, game);
        respondJson(he, 201, json);
    }

    // Handles the GET request to retrieve a created game by id.
    // id - the id of the game.
    private void handleGetGame(HttpExchange he, String id) throws IOException {
        Penkiolika game = games.get(id);
        if (game == null) {
            respondError(he, 404, "Game with id=" + id + " not found");
        } else {
            JSONObject json = this.getGameJson(id, game);
            respondJson(he, 200, json);
        }
    }

    // Handles the PATCH request to make a move in the game.
    // id - the id of the game of the move.
    // inJson - the JSON specifying the move.
    private void handlePatchDoMove(HttpExchange he, String id, JSONObject inJson) throws IOException {
        if (inJson.has(JSON_MOVE)) {
            String move = inJson.getString(JSON_MOVE);
            Function<Penkiolika, Boolean> moveFun = null;
            if (move.equals(JSON_MOVE_LEFT)) {
                moveFun = game -> game.moveLeft();
            } else if (move.equals(JSON_MOVE_RIGHT)) {
                moveFun = game -> game.moveRight();
            } else if (move.equals(JSON_MOVE_TOP)) {
                moveFun = game -> game.moveTop();
            } else if (move.equals(JSON_MOVE_BOTTOM)) {
                moveFun = game -> game.moveBottom();
            }
            if (moveFun == null) {
                respondError(he, 400, "Unknown move " + move);
            } else {
                Penkiolika game = games.get(id);
                if (game == null) {
                    respondError(he, 404, "Game with id=" + id + " not found");
                } else if (moveFun.apply(game)) {
                    JSONObject json = this.getGameJson(id, game);
                    respondJson(he, 200, json);
                } else {
                    respondError(he, 409, "Unable to move " + move + " in game with id=" + id);
                }
            }
        } else {
            respondError(he, 400, "Move must be provided");
        }
    }

    // Handles the DELETE request to remove the game.
    // id - the id of the game to be deleted.
    private void handleDeleteGame(HttpExchange he, String id) throws IOException {
        Penkiolika game = games.remove(id);
        if (game == null) {
            respondError(he, 404, "Game with id=" + id + " not found");
        } else {
            JSONObject json = this.getGameJson(id, game);
            respondJson(he, 200, json);
        }
    }

    // Convenience method to check if the request path matches the expected value.
    // The first element of the path is ommited, because the full request path
    // is expected to start by "/" and due to how String.split(String) method works.
    // Other than that, request path is matched element by element to the matcher.
    // If the matcher's element is null, then the respective element of the path
    // might be anything. Otherwise it must match exactly.
    // receivedPath - full request path split by "/" character
    // pathToMatch - matcher array
    private boolean pathMatches(String[] receivedPath, String[] pathToMatch) {
        if (receivedPath.length == pathToMatch.length + 1 && receivedPath[0].equals("")) {
            for (int i=0; i<pathToMatch.length; i++) {
                if ((pathToMatch[i] != null) && !(pathToMatch[i].equals(receivedPath[i+1]))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    // Convenience method to form a game JSON object.
    // id - id of the game
    // game - the game, which should be converted to JSON.
    private JSONObject getGameJson(String id, Penkiolika game) {
        JSONObject result = new JSONObject();
        result.put(JSON_ID, id);
        synchronized(game) {
            result.put(JSON_BOARD, new JSONArray(game.getBoard()));
            result.put(JSON_FINAL, game.isFinal());
        }
        return result;
    }

    // Convenience method to respond to the client by provided status code and
    // JSON object.
    // statusCode - status code of the HTTP response.
    // json - contents JSON of the response.
    private void respondJson(HttpExchange he, int statusCode, JSONObject json) throws IOException {
        byte[] response = json.toString(4).getBytes();
        he.sendResponseHeaders(statusCode, response.length);
        try (OutputStream os = he.getResponseBody()) {
            os.write(response);
            os.flush();
        } catch (IOException ioe) {
            System.out.println("IOException while sendng response status=" + statusCode +
                    ", contents=" + json.toString() + " to client. Reason=" + ioe.getMessage());
        }
    }

    // Convenience method to log the error, form the error JSON response and
    // respond with it to the client.
    // statusCode - status code of the HTTP response.
    // reason - error reason message.
    private void respondError(HttpExchange he, int statusCode, String reason) throws IOException {
        System.out.println("Responding error " + statusCode + ": " + reason);
        JSONObject json = new JSONObject();
        json.put(JSON_ERROR_REASON, reason);
        this.respondJson(he, statusCode, json);
    }
}
