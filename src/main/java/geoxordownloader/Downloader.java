package geoxordownloader;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;

/**
 * Handles all program execution.
 * 
 * @author Nickolas S. Bradham
 *
 */
public final class Downloader {

	private static final File OUT_DIR = new File("Downloads");
	private final String[] urls;
	private byte ind = 0;

	/**
	 * Constructs a new Downloader and retrieves all file URLs from site.
	 * 
	 * @param src
	 * 
	 * @throws IOException Thrown by {@link org.jsoup.Connection#get()}.
	 */
	private Downloader(String src) throws IOException {

		ArrayList<String> tmp = new ArrayList<>();

		Jsoup.parse(src).getElementsByClass("flex flex-col").forEach(e -> tmp.add(e.getElementsByClass(
				"rounded-full flex items-center gap-2 border-2 border-accent select-none bg-accent h-min text-white px-4 py-2")
				.get(0).attr("href")));

		urls = tmp.toArray(new String[0]);
	}

	/**
	 * Retrieves the next file URL to download.
	 * 
	 * @return The URL String of the file.
	 */
	private synchronized String next() {

		if (ind < urls.length)
			return urls[ind++];

		return null;
	}

	/**
	 * Downloads all files.
	 * 
	 * @throws InterruptedException Thrown by {@link Thread#join()}.
	 */
	private void start() throws InterruptedException {

		OUT_DIR.mkdir();

		Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];

		for (byte i = 0; i < threads.length; i++) {

			threads[i] = new Thread(() -> {

				String url;
				while ((url = next()) != null) {

					System.out.println("Downloading: " + url);

					try {

						FileOutputStream fos = new FileOutputStream(
								new File(OUT_DIR, url.substring(url.lastIndexOf('/') + 1)));

						fos.getChannel().transferFrom(Channels.newChannel(new URL(url).openStream()), 0,
								Long.MAX_VALUE);

						fos.close();

					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error downloading: " + url + "\n" + e1, "Error occured.",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			threads[i].start();
		}

		for (Thread t : threads)
			t.join();

		JOptionPane.showMessageDialog(null, "Successfully downloaded all songs.");
	}

	/**
	 * Constructs and starts a new {@link Downloader} instance.
	 * 
	 * @param args Ignored.
	 * @throws IOException          Thrown by {@link #Downloader()}.
	 * @throws InterruptedException Thrown by {@link #start()}.
	 * @throws URISyntaxException   Thrown by {@link URI#URI(String)}.
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
		Desktop.getDesktop().browse(new URI("https://beta.geoxor.moe/downloads/songs"));
		String src = JOptionPane.showInputDialog("Paste page sourcecode here:");
		if (src != null)
			new Downloader(src).start();
	}
}