package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.WebWalking;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.DynamicClicking;
import org.tribot.script.interfaces.Painting;
import org.tribot.api2007.Player;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Walking;

import java.awt.RenderingHints;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Game;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.types.RSItem;

// Paint Imports
import java.awt.Color; 
import java.awt.Font;
import java.awt.Graphics; 
import java.awt.Graphics2D; 
import java.awt.Image;


@ScriptManifest(authors = {"botsallday"}, category = "Money Making", name = "FlawlessEssenceMiner")

public class FlawlessEssenceMiner extends Script implements Painting {
    
    // set variables
	private ABCUtil abc = new ABCUtil();
	private GUI gui = new GUI();
    private final Image img = null;
    private boolean has_pic = false;
    private static final long startTime = System.currentTimeMillis();
    private int essence_mined = 0;
    private int current_ess = 0;
    private RSObject target_rock;
    private boolean execute = true;
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);
    private String antiban_status = "Waiting";

    private final RSArea bank_area = new RSArea(new RSTile(3253, 3420, 0), new RSTile(3250, 3422, 0));
    private final RSArea teleport_area = new RSArea(new RSTile(3252, 3401, 0), new RSTile(3254, 3402, 0));
    
    public boolean inventoryIsEmpty() {
    	if (Inventory.getAll().length > 0) {
    		return false;
    	}
    	
    	return true;
    }
    
    public RSTile getTile(boolean use_bank) {
    	// check run since we are about to walk
    	checkRun();
    	General.sleep(400, 1500);
    	
    	if (use_bank) {
    		return bank_area.getRandomTile();
    		
    	} 
    	
    	return teleport_area.getRandomTile();
    }
    
    public Object getPickaxe() {
    	return gui.getPicaxeValue();
    }
    
    public void run() {
    	General.useAntiBanCompliance(true);
    	gui.setVisible(true);
    	execute = false;
    	while (gui.getWaitGui()){
    		General.sleep(100, 150);
    		println("Sleeping for gui");
    	}
    	
    	println("Gui loaded");
    	
    	execute = true;
    	
    	gui.setVisible(false);
    	
        while(execute) {
            State state = state();
            if (state != null) {
                switch (state) {
                    case WALK_TO_TELEPORT:
                    	log("Walking to rune shop for teleport");
                        if (canTeleport()) {
                        	tryTeleport();
                        };
                        break;
                    case WALK_TO_PORTAL:
                    	RSNPC[] portal = NPCs.getAll();
                    	
                    	log("Walking to portal to leave ess mine");
                    	if (portal.length > 0) {
                    		if (portal[0].isOnScreen()) {
                    			if (DynamicClicking.clickRSNPC(portal[0], "Use")) {
                                    Timing.waitCondition(new Condition() {
                                        @Override
                                        public boolean active() { //it will loop this
                                            General.sleep(100, 200);
                                            return Objects.find(20, "Rune Essence").length == 0; //until we are teleported to the mine
                                        }
                                    }, General.random(4000, 8000));
                                } else if (DynamicClicking.clickRSNPC(portal[0], "Exit")) {
	                                Timing.waitCondition(new Condition() {
	                                    @Override
	                                    public boolean active() { //it will loop this
	                                        General.sleep(100, 200);
	                                        return Objects.find(20, "Rune Essence").length == 0; //until we are teleported to the mine
	                                    }
	                                }, General.random(4000, 8000));
                                }
                    		} else {
                    			WebWalking.walkTo(portal[0].getPosition());
                    		}
                    	}
                    	break;
                    case WALK_TO_ROCKS:
                    	log("Walking to rocks to mine ess");
                    	final RSObject[] rocks = Objects.findNearest(30, "Rune Essence");
                    	
                    	if (!tryWebWalkToRock() && rocks.length > 0) {
                    		println("Failed to webwalk to rock, trying blind walk");
                    		if (rocks[0].isOnScreen()) {
                        		Walking.blindWalkTo(rocks[0].getPosition());
                    		}
                    	};
                    	break;
                    case WALK_TO_BANK:
                    	log("Walking to bank");
                    	println("About to go");
                    	WebWalking.walkTo(getTile(true));
                        break;
                    case GET_PIC:
                    	log("Attempting to get a pickaxe");
                    	getPic();
                    	break;
                    case DEPOSIT_ITEMS:
                    	log("Depositing items in bank");
                        handleBanking();
                        break;
                    case MINE_ROCKS:
                        mineRock(target_rock);
                        break;
                    case WALKING:
                    	handleWait();
                		General.sleep(1500, 7500);
                    	break;
                    case ANTI_BAN:
                    	handleWait();
                		General.sleep(200, 800);
                    	break;
                    case SOMETHING_WENT_WRONG:
                    	log("Stopping script, something went wrong");
                    	execute = false;
                    	break;
                }
            }
            General.sleep(892,  2134);
        }
    }
    
    private boolean tryWebWalkToRock() {
    	final RSObject[] rocks = Objects.findNearest(30, "Rune Essence");
    	
    	if (rocks.length > 1 && abc.BOOL_TRACKER.USE_CLOSEST.next()) {
    		if ((rocks[1].getPosition().distanceToDouble(Player.getPosition()) - rocks[0].getPosition().distanceTo(Player.getPosition())) < 5) {
            	abc.BOOL_TRACKER.USE_CLOSEST.reset();
    			return WebWalking.walkTo(rocks[1].getPosition());
    		}
    	} else if (rocks.length > 0) {
    		return WebWalking.walkTo(rocks[0].getPosition());
    	}
    	
    	return false;
    }

    private State state() {
        // whether or not we need to bank is the variable that drives the script
        RSNPC[] portals = NPCs.getAll();
        
        if (Inventory.find(gui.getPicaxeValue()).length > 0) {
        	has_pic = true;
        } else {
        	has_pic = false;
        }
        
        if (Inventory.find("Rune Essence", "Pure Essence").length > 0) {
        	if (Inventory.getCount("Rune Essence", "Pure Essence") > current_ess) {
        		current_ess = Inventory.getCount("Rune Essence", "Pure Essence");
        		essence_mined ++;
        	};
        }
        
        if (Inventory.isFull() && !Banking.isBankScreenOpen() && !Player.isMoving() && Objects.findNearest(20, "Rune Essence").length == 0) {
             if (Banking.isInBank()) {
                 return State.DEPOSIT_ITEMS;
             } else {
            	 handleWait();
                 return State.WALK_TO_BANK;
             }
        } else if (has_pic && !Inventory.isFull() && !Banking.isBankScreenOpen() && !Player.isMoving() && Objects.findNearest(20, "Rune Essence").length > 0 && Player.getAnimation() == -1) {
            RSObject[] nearest_rock = Objects.findNearest(3, "Rune Essence");

            if (nearest_rock.length > 0) {
            	target_rock = nearest_rock[0];
            	// anti ban compliance
                if (nearest_rock.length > 1 && this.abc.BOOL_TRACKER.USE_CLOSEST.next()) {
                    if (nearest_rock[1].getPosition().distanceToDouble(nearest_rock[0]) < 3.0)
                        target_rock = nearest_rock[1];
                }
                
                if (abc.BOOL_TRACKER.HOVER_NEXT.next()) {
                	target_rock.hover();
                }
                
                return State.MINE_ROCKS;
            }  else {
                return State.WALK_TO_ROCKS;
            }
        } else if (Player.isMoving()) {
        	return State.WALKING;
        } else if (has_pic && !Player.isMoving() && Objects.findNearest(20, "Rune Essence").length == 0) {
        	log("No essence in range");
        	println(Objects.findNearest(20, "Rune Essence"));
        	return State.WALK_TO_TELEPORT;
        } else if (Inventory.isFull() && !Player.isMoving() && !Banking.isBankScreenOpen() && portals.length > 0) {
        	return State.WALK_TO_PORTAL;
        } else if (!has_pic){
        	return State.GET_PIC;
    	} else if (Player.getAnimation() > -1) {
        	return State.ANTI_BAN;
        }
        // if we dont satisfy any of the above conditions, we may have a problem
        return State.SOMETHING_WENT_WRONG;

        
    }

   enum State {
        WALK_TO_TELEPORT,
        WALK_TO_BANK,
        WALK_TO_ROCKS,
        MINE_ROCKS,
        DEPOSIT_ITEMS,
        SOMETHING_WENT_WRONG,
        WALK_TO_PORTAL,
        WALKING,
        GET_PIC,
        ANTI_BAN
    }
   
   private boolean tryTeleport() {
	   RSNPC[] npc = NPCs.find("Aubury");

	   if (npc.length > 0 && npc[0].getActions().length > 0 && npc[0].isOnScreen()) {
		   RSNPC harry_potter = npc[0];
		   log("Trying to get harry potter to teleport us");
           if (DynamicClicking.clickRSNPC(harry_potter, "Teleport")) {
               Timing.waitCondition(new Condition() {
                   @Override
                   public boolean active() { //it will loop this
                       General.sleep(100, 200);
                       return Objects.find(20, "Rune Essence").length > 0; //until we are teleported to the mine
                   }
               }, General.random(4000, 8000));
           }
	   }
	   
	   return false;
   }
   
   private boolean canTeleport() {
		RSNPC[] npc = NPCs.find("Aubury");

		log("Searching for harry potter");
		if (npc.length > 0 && npc[0].getPosition().distanceToDouble(Player.getPosition()) > 4) {
			log("Walking to harry potters");
			return WebWalking.walkTo(npc[0].getPosition());
		} else if (npc.length > 0) {
			return true;
		} else {
			WebWalking.walkTo(getTile(false));
		}
		
		return false;

   }
   
    private boolean depositAll() {
    	if (Inventory.isFull()) {
	        int items_deposited = Banking.depositAll();
        	General.sleep(845, 3558);

	        // if we deposited any items, print the number
	        if (items_deposited > 0) {
	        	if (Inventory.find(gui.getPicaxeValue()).length == 0) {
	        		getPic();
	        		General.sleep(100, 640);
	        	}
	        	current_ess = 0;
	            log("Deposited "+ items_deposited +" items.");
	            // close bank
	            closeBankScreen();
	        }
        
	        return true;
    	} else {
    		return false;
    	}
    }
    
    private void getPic() {
    	RSItem[] pic = Banking.find(gui.getPicaxeValue());
    	
    	if (Banking.openBank()) {
    		if (pic.length > 0) {
    			if (!Inventory.isFull() && Inventory.find(gui.getPicaxeValue()).length == 0) {
    				Banking.withdraw(1, gui.getPicaxeValue());
    			} else {
    				depositAll();
    			}
    		}
    	}
    }
    
    private boolean handleBanking() {
        // we know we are in the bank, so try to open bank screen
        boolean bank_screen_is_open = Banking.openBank();
        println("Banking");
        if (bank_screen_is_open) {
        	println("Banking its open");
            return depositAll();
        }
        
        return false;
    }

    private void closeBankScreen() {
        if (Banking.isBankScreenOpen()) {
            Banking.close();
        }
    }

    private void mineRock(RSObject rock) {
    	if (!Inventory.isFull()) {
	        if (rock.isOnScreen() && rock.isClickable()) {
	        	DynamicClicking.clickRSObject(rock, "mine");
	            
	            abc.BOOL_TRACKER.HOVER_NEXT.reset();
	            abc.BOOL_TRACKER.USE_CLOSEST.reset();
	            // item interaction delay
	            General.sleep(abc.DELAY_TRACKER.ITEM_INTERACTION.next());
	        }
    	}
    }

    private void log(String message) {
        println(message);
    }

    public void onPaint(Graphics g) {
        // setup image
        Graphics2D gg = (Graphics2D)g;
        gg.setRenderingHints(aa);
        gg.drawImage(img, 0, 338, null);
        // set variables for display
        long run_time = System.currentTimeMillis() - startTime;
        int ess_per_hour = (int)(essence_mined * 3600000 / run_time);
    
        g.setFont(font);
        g.setColor(new Color(200, 100, 20));
        g.drawString("Runtime: " + Timing.msToString(run_time), 330, 395);
        g.drawString("Ess Mined: " + essence_mined, 330, 415);
        g.drawString("Ess Per Hour: "+ ess_per_hour, 330, 435);
        g.drawString("Antiban Status: "+antiban_status, 330, 375);
    }

    private void checkRun() {
    	final int run_energy = Game.getRunEnergy();
    	if (run_energy >= abc.INT_TRACKER.NEXT_RUN_AT.next() && !Game.isRunOn()) {
    		log("Turning run on");
    		WebWalking.setUseRun(true);
    		abc.INT_TRACKER.NEXT_RUN_AT.reset();
    	}
    }
    
    private boolean performTabAntiBan(long next, GameTab.TABS tab) {
    	
		if (System.currentTimeMillis() >= next && GameTab.getOpen() != tab) {
			log("Performing check tab anti ban");
			if (GameTab.open(tab)) {
				antiban_status = "Performing antiban action";
				log("Successfully performed check tab "+"("+tab+") antiban");
				return true;
			};
			
			return true;
		}
			
		return false;
		
    }
    
    private void handleWait() {
    	antiban_status = "Checking";
    	
    	if (Player.isMoving() || Player.getAnimation() != -1 && General.random(1, 100) >= 98) {
    		antiban_status = "Performing antiban action";
    		// control cpu usage
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
    	} else if (General.random(1, 100) > 50) {
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
    	
    	antiban_status = "Waiting";
    }


}