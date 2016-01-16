package scripts.BADFlawlessEssenceMiner.flawlessessenceminer;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.api.General;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.Starting;

import scripts.BADFlawlessEssenceMiner.framework.flawlessessenceminer.FlawlessEssenceMinerCore;
import scripts.BADFlawlessEssenceMiner.framework.paint.BADPaint;

import java.awt.Graphics; 

@ScriptManifest(authors = {"botsallday"}, category = "Mining", name = "FlawlessEssMiner")

public class FlawlessEssenceMiner extends Script implements Painting, Starting {
    
	private BADPaint painter = new BADPaint();
	private FlawlessEssenceMinerCore core = new FlawlessEssenceMinerCore();
    
    public void run() {
		// execute the script
		core.run();
    }
   
    public void onPaint(Graphics g) {
    	painter.paint(g, core.getEssenceMined());
    }

	@Override
	public void onStart() {
    	General.useAntiBanCompliance(true);
	}
}