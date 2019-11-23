import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Formatter;
import java.util.Random;

/**
 * Tiny Echo Server Ping UDP
 * using blocking DatagramChannel
 * 
 * @author Tommaso Macchioni
 *
 */
public class Server {

	
	private final static double UNFAV_ODDS = 0.25; //Probabilità di scartare un pacchetto

	public static void main(String[] args) {

		//Controllo argomenti
		if(args.length != 2) {
			System.out.println("ERR -arg 1 2");
			return;
		} 

		int port = -1;

		try { port = Integer.parseInt(args[0]); }
		catch(NumberFormatException ex) {
			System.out.println("ERR -arg 1");
			return;
		}


		long seed = 0;

		try { seed = Long.parseLong(args[1]); }
		catch(NumberFormatException ex) {
			System.out.println("ERR -arg 2");
			return;
		}

		
		try {
			Random rand = new Random(seed);

			DatagramChannel channel = DatagramChannel.open();
			channel.socket().bind(new InetSocketAddress(port));

			ByteBuffer buf = ByteBuffer.allocate(20);
			Formatter formatter = null;
			
			for(int i=0; i<10; i++) { //ciclo ricezione di 10 messaggi
				
				formatter = new Formatter();
				
				buf.clear();
				SocketAddress addrClient = channel.receive(buf);

				formatter.format("%s:%s %s", ((InetSocketAddress) addrClient).getHostString(), ((InetSocketAddress) addrClient).getPort(), new String(buf.array()));

				if(rand.nextDouble() < UNFAV_ODDS) { //Simulazione scarto di un pacchetto con probabilità UNDAV_ODDS
					formatter.format(" - PING non inviato");
					System.out.println(formatter.toString());
					continue;
				} else { //Invio del pacchetto e simulazione di delay millisecondi
					double delay = rand.nextDouble()*2000;
					Thread.sleep((long)delay);
					buf.flip();
					channel.send(buf, addrClient);
					formatter.format(" - PING ritardato di %dms", (long) delay);
					System.out.println(formatter.toString());
				}
				
			}

		}catch(IOException e) {
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
	}

}
