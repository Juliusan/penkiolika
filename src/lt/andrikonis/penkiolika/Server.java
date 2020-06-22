/*
 * Technical task for Danske bankas
 */
package lt.andrikonis.penkiolika;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Class, for starting the HTTP server. It provides two ways to do it:
 * <ol>
 *  <li> using {@link #main} method. Starts the game server using integrated
 *       request handler.
 *  <li> using {@link #start} method. Starts the HTTP server using port, base path
 *       and request handler provided as parameters.
 * </ol>
 *
 *
 * @author julius
 */
public class Server {

    /**
     *  Default port for server to listen to: 8080.
     */
    public static final int DEFAULT_SERVER_PORT = 8080;

    /**
     * Starts the game server.
     *
     * @param args the command line arguments. Only the first argument is used
     * and it is optional. If it is provided, it should be integer number meaning
     * the port number on which the server should listen for requests. If it is
     * not provided, {@link #DEFAULT_SERVER_PORT} is used.
     *
     * @throws java.io.IOException if input output exception occurs during server
     * creation. For details see {@link com.sun.net.httpserver.HttpServer#create(java.net.InetSocketAddress, int)}
     * documentation.
     */
    public static void main(String[] args) throws IOException {
        int port;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException nfe) {
                System.out.println("Integer as a parameter expected, and " + args[0] + " received." +
                        "Assuming port=" + DEFAULT_SERVER_PORT);
                port = DEFAULT_SERVER_PORT;
            }
        } else {
            System.out.println("No parameter provided. Assuming port=" + DEFAULT_SERVER_PORT);
            port = DEFAULT_SERVER_PORT;
        }
        Server.start(port, ServerHandler.BASE_PATH, new ServerHandler());
    }

    /**
     * Starts the HTTP server, which listens to provided port and responds according
     * to provided request handler.
     *
     * @param port port, for server to listen to requests.
     * @param basePath base path for requests to the server.
     * @param handler request handler object for this server.
     * @return reference to the started server.
     *
     * @throws java.io.IOException if input output exception occurs during server
     * creation. For details see {@link com.sun.net.httpserver.HttpServer#create(java.net.InetSocketAddress, int)}
     * documentation.
     */
    public static HttpServer start(int port, String basePath, HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(basePath, handler);
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port=" + port);
        return server;
    }

}
