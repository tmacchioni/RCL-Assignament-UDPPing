import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Tiny Client Ping UDP
 * using blocking DatagramSocket
 * 
 * @author Tommaso Macchioni
 *
 */
public class Client {


	public static void main(String[] args) {

		//Controllo argomenti
		if(args.length != 2) {
			System.out.println("ERR -arg 1 2");
			return;
		} 

		InetAddress serverIA = null;

		try {
			serverIA = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
			System.out.println("ERR -arg 1");
			return;
		}

		int port = -1;

		try {
			port = Integer.parseInt(args[1]);
		}catch(NumberFormatException ex) {
			System.out.println("ERR -arg 2");
			return;
		}
		
		//Some variables
		int numPacketsSend = 0;
		int numPacketsReceive = 0;
		long rttMin = 2000;
		long rttMax = 0;
		long sumOfAllRtt = 0;
		long startTime = 0;
		long endTime = 0;
		long rtt = 0;

		try(DatagramSocket clientSocket = new DatagramSocket()) {

			InetSocketAddress isa = new InetSocketAddress(serverIA, port);

			byte[] buf = null;
			DatagramPacket dgp = new DatagramPacket(new byte[20], 0, isa);

			clientSocket.setSoTimeout(2000); //la receive si sblocca dopo 2 sec

			String msg = null;

			
			for(int seqno=0; seqno<10; seqno++) { //ciclo invio di 10 msg

				//Invio pacchetto PING seqno timestamp
				startTime = System.currentTimeMillis();
				msg = String.format("PING %d %d", seqno, startTime);

				buf = msg.getBytes();

				dgp.setData(buf);
				dgp.setLength(buf.length);

				clientSocket.send(dgp);
				numPacketsSend++;
				
//				System.out.print(new String(dgp.getData()));

				//Ricezione pacchetto
				try {
					clientSocket.receive(dgp);
					
					endTime = System.currentTimeMillis();					
					rtt = (endTime - startTime); //Calcolo Round Trip Time
					System.out.println(msg + " RTT: " + rtt + "ms");

					//Aggiornamento variabili
					numPacketsReceive++;
					sumOfAllRtt += rtt;
					if(rtt < rttMin) rttMin = rtt;
					if(rtt > rttMax) rttMax = rtt;

				}catch(SocketTimeoutException ex) { //E' scaduto il timeout
					System.out.println(msg + "*");
				}


			}

		}catch (IOException e) {
			e.printStackTrace();
			return;
		}

		//Stampa finale
	
		double packetLoss = ((numPacketsSend-numPacketsReceive)/((double)numPacketsSend))*100;
		
		double rttAvg = sumOfAllRtt/numPacketsSend;
		
		System.out.println(
				String.format("\n---- PING Statistics ----\n"
						+ "%d packets transmitted, %d packets received, %.0f%% packet loss\n"
						+ "rtt min/avg/max = %d/%.2f/%d ms",
						numPacketsSend, numPacketsReceive, packetLoss, rttMin, rttAvg, rttMax)
				);
		
	}


}
