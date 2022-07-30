package geoxordownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.ArrayList;

import org.jsoup.Jsoup;

/**
 * Handles all program execution.
 * 
 * @author Nickolas S. Bradham
 *
 */
final class Downloader {

	private static final File OUT_DIR = new File("Downloads");
	private final String[] urls;
	private byte ind = 0;

	/**
	 * Constructs a new Downloader and retrieves all file URLs from site.
	 * 
	 * @throws IOException Thrown by {@link org.jsoup.Connection#get()}.
	 */
	private Downloader() throws IOException {

		ArrayList<String> tmp = new ArrayList<>();

		Jsoup.connect("https://beta.geoxor.moe/downloads/songs").get().getElementsByClass("flex flex-col")
				.forEach(e -> tmp.add(e.getElementsByClass(
						"rounded-full flex items-center gap-2 border-2 border-accent select-none bg-accent h-min text-white px-4 py-2")
						.get(0).attr("href")));

		urls = tmp.toArray(new String[tmp.size()]);
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
					}
				}
			});

			threads[i].start();
		}

		for (Thread t : threads)
			t.join();

		System.out.println("Done.");
	}

	/**
	 * Constructs and starts a new {@link Downloader} instance.
	 * 
	 * @param args Ignored.
	 * @throws IOException          Thrown by {@link #Downloader()}.
	 * @throws InterruptedException Thrown by {@link #start()}.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		new Downloader().start();
	}
}