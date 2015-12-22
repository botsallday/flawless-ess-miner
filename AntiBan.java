package scripts;

import org.tribot.api.General;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Player;

public class AntiBan {
	
	public String antiban_status;
	public long antiban_performed;
	public ABCUtil abc;
	
    public AntiBan() {
    	log("Starting antiban");
    	abc = new ABCUtil();
    	antiban_performed = 0;
    	antiban_status = "Waiting";
    }
	
    private boolean performTabAntiBan(long next, GameTab.TABS tab) {
    	
		if (System.currentTimeMillis() >= next && GameTab.getOpen() != tab) {
			log("Performing check tab anti ban");
			if (GameTab.open(tab)) {
				antiban_status = "Performing antiban action";
				antiban_performed ++;
				log("Successfully performed check tab "+"("+tab+") antiban");
				return true;
			};
			
			return true;
		}
			
		return false;
		
    }
    
    private void log(String string) {
    	System.out.println(string);
	}

	public void handleWait() {
    	antiban_status = "Checking";
    	
    	if (Player.isMoving() || Player.getAnimation() != -1 && General.random(1, 100) >= 98) {
    		checkGameTabAntiBan();
    	} else if (General.random(1, 100) > 50) {
    		checkMouseAntiBan();
    	}
    	
    	antiban_status = "Waiting";
    }
	
	public void handleWalkingTimeout() {
		
	}
	
	public void checkMouseAntiBan() {
        if (System.currentTimeMillis() >= abc.TIME_TRACKER.EXAMINE_OBJECT.next()) {
			log("Examine object antiban");
			abc.performExamineObject();
		}

        if (System.currentTimeMillis() >= abc.TIME_TRACKER.ROTATE_CAMERA.next()) {
            log("Performing rotate camera anti ban");
            abc.performRotateCamera();
        }

        if (System.currentTimeMillis() >= abc.TIME_TRACKER.PICKUP_MOUSE.next()) {
            log("Performing pickup mouse anti ban");
            abc.performPickupMouse();
        }

        if (System.currentTimeMillis() >= abc.TIME_TRACKER.LEAVE_GAME.next()) {
            log("Performing mouse leave game anti ban");
            abc.performLeaveGame();
        }

        if (System.currentTimeMillis() >= abc.TIME_TRACKER.RANDOM_MOUSE_MOVEMENT.next()) {
            log("Performing mouse movement anti ban");
            abc.performRandomMouseMovement();
        }

        if (System.currentTimeMillis() >= abc.TIME_TRACKER.RANDOM_MOUSE_MOVEMENT.next()) {
            log("Performing mouse right click anti ban");
            abc.performRandomRightClick();
        }
	}
	
	public void checkGameTabAntiBan() {
        switch (General.random(1, 5)) {
	        case 1:
	            if (performTabAntiBan(abc.TIME_TRACKER.CHECK_EQUIPMENT.next(), GameTab.TABS.EQUIPMENT)) {
	                abc.TIME_TRACKER.CHECK_EQUIPMENT.reset();
	            };
	            break;
	        case 2:
	
	            if (performTabAntiBan(abc.TIME_TRACKER.CHECK_FRIENDS.next(), GameTab.TABS.FRIENDS)) {
	                abc.TIME_TRACKER.CHECK_FRIENDS.reset();
	            };
	            break;
	
	        case 3:
	
	            if (performTabAntiBan(abc.TIME_TRACKER.CHECK_COMBAT.next(), GameTab.TABS.COMBAT)) {
	                abc.TIME_TRACKER.CHECK_COMBAT.reset();
	            };
	            break;
	
	        case 4:
	
	            if (performTabAntiBan(abc.TIME_TRACKER.CHECK_MUSIC.next(), GameTab.TABS.MUSIC)) {
	                abc.TIME_TRACKER.CHECK_MUSIC.reset();
	            };
	            break;
	
	        case 5:
	
	            if (performTabAntiBan(abc.TIME_TRACKER.CHECK_QUESTS.next(), GameTab.TABS.QUESTS)) {
	                abc.TIME_TRACKER.CHECK_QUESTS.reset();
	            };
	            break;
	    }
	}
}
