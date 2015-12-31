package scripts;

import org.tribot.api.General;
import org.tribot.api2007.Camera;
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
import org.tribot.api2007.util.DPathNavigator;

public class Transportation {
	
	private AntiBan anti_ban;
	
	
	Transportation() {
		anti_ban = new AntiBan();
	}
	
    public void checkRun() {
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
	
	public void iteratePath(RSTile[] path) {
		General.println("Printing Path");
		for (int i=0; i<path.length; i++) {
			General.println(path[i].toString());
		}
	}
	
	public DPathNavigator nav() {
		return new DPathNavigator();
	}
	
	public boolean walkCustomNavPath(RSTile end) {
		return Walking.walkPath(nav().findPath(end));
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
    
	public boolean dpathnavWalk(RSTile end) {
		final DPathNavigator nav = new DPathNavigator();
		return nav.traverse(end);
	}
	
	public boolean webWalking(RSTile end) {
		return WebWalking.walkTo(end);
	}
	
	public boolean normalWalk(RSTile end) {
		return Walking.walkTo(end);
	}
	
	public boolean walkScreenPath(RSTile[] path) {
		return Walking.walkScreenPath(path);
	}
	
	public RSTile[] generateScreenPath(RSTile end, boolean walk_it) {
		if (walk_it) {
			walkScreenPath(Walking.generateStraightScreenPath(end));
		}
		
		return Walking.generateStraightScreenPath(end);
	}
	
	public boolean clickScreenTile(RSTile tile) {
		if (tile.isOnScreen()) {
			return Walking.clickTileMS(tile, 0);
		}
		
		return false;
	}
	
	public boolean clickMinimapTile(RSTile tile) {
		if (tile.isOnScreen()) {
			return Walking.clickTileMM(tile, 0);
		}
		
		return false;
	}
	
	public boolean rotateCameraAndWalkToTile(RSTile tile) {
		Camera.turnToTile(tile);
		return webWalking(tile);
	}
}
