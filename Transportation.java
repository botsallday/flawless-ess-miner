package scripts;

import org.tribot.api2007.Game;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;

public class Transportation {
	
	private AntiBan anti_ban;
	
	
	Transportation() {
		anti_ban = new AntiBan();
	}
	
    private void checkRun() {
    	if (Game.getRunEnergy() >= anti_ban.abc.INT_TRACKER.NEXT_RUN_AT.next() && !Game.isRunOn()) {
    		System.out.println("Turning run on");
    		WebWalking.setUseRun(true);
    		anti_ban.abc.INT_TRACKER.NEXT_RUN_AT.reset();
    	}
    }  
    
    public RSTile getTile(RSArea area, boolean check_run) {
    	// check run since we are about to walk
    	if (check_run) {
    		checkRun();
    	}
    	
    	return area.getRandomTile();
    }
    
    public RSArea getAreaFromCoords(int x_min, int x_max, int y_min, int y_max, int floor) {
    	return new RSArea(new RSTile(x_min, y_min, floor), new RSTile(x_max, y_max, floor));
    }
    
    public boolean customWalkPath(RSTile start, RSTile end, boolean is_object) {
    	if (PathFinding.generatePath(start, end, is_object).length > 0) {
    		System.out.println("Walking custom path");
    		return Walking.walkPath(PathFinding.generatePath(start, end, is_object));
    	};
    	
    	return false;
    }
    
    public boolean blindWalkToObject(RSObject[] obj) {
		if (obj[0].isOnScreen() && validateWalk(obj[0].getPosition(), true)) {
			return Walking.blindWalkTo(obj[0].getPosition());
		}
		
		return false;
	}
    
    public boolean blindWalkToNpc(RSNPC[] npcs) {
        if (npcs.length > 0 && validateWalk(npcs[0].getPosition(), false)) {
            return Walking.blindWalkTo(npcs[0].getPosition());
        }
        
        return false;
    }
    
    public boolean webWalkToObject(String object_name) {
        final RSObject[] obj = Objects.findNearest(30, object_name);
        
        if (obj.length > 1 && anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.next()) {
            if (obj[1].getPosition().distanceToDouble(Player.getPosition()) <= (obj[0].getPosition().distanceTo(Player.getPosition()) + 5) && validateWalk(obj[1].getPosition(), true)) {
                anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.reset();
                return WebWalking.walkTo(obj[1].getPosition());
            }
        } else if (obj.length > 0 && validateWalk(obj[0].getPosition(), true)) {
            return WebWalking.walkTo(obj[0].getPosition());
        }
        
        return false;
    }
    
    public boolean webWalkToNpc(String npc_name) {
        final RSNPC[] obj = NPCs.find(npc_name);
        
        if (obj.length > 0 && validateWalk(obj[0].getPosition(), false)) {
            return WebWalking.walkTo(obj[0].getPosition());
        }
        
        return false;
    }
    
    public boolean validateWalk(RSTile start, RSTile end, boolean accept_adjacent) {
    	return PathFinding.canReach(start, end, true);
    }
    
    public boolean validateWalk(RSTile end, boolean accept_adjacent) {
    	return PathFinding.canReach(Player.getPosition(), end, true);
    }
    
}
