package scripts.BADFlawlessEssenceMiner.framework.paint;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

import org.tribot.api.General;
import org.tribot.api.Timing;

public class BADPaint {
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);
    private final Image img = getImage("http://i.imgur.com/emwR76O.png");
    private static final long startTime = System.currentTimeMillis();
    
    public Image getImage(String url) {
        // get paint image
        try {
        	General.println("Getting image");
        	General.println(url);
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
        	General.println("Failed to get image");
            return null;
        }
    }
    
	public void paint(Graphics g, long essence_mined) {
			
        // setup image
        Graphics2D gg = (Graphics2D)g;
        gg.setRenderingHints(aa);
        gg.drawImage(img, 0, 338, null);
        // set variables for display
        long run_time = System.currentTimeMillis() - startTime;
        int ess_per_hour = (int)(essence_mined * 3600000 / run_time);
    
        g.setFont(font);
        g.setColor(new Color(0, 0, 0));
        g.drawString("" + Timing.msToString(run_time), 95, 393);
        g.drawString("" + essence_mined, 141, 417);
        g.drawString(""+ ess_per_hour, 160, 440);
        g.drawString(""+(essence_mined*13), 80, 463);

	}
}
