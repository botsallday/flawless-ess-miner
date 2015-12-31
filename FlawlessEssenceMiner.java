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
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.WebWalking;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.DynamicClicking;
import org.tribot.script.interfaces.Painting;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Walking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.ext.Filters.GroundItems;

import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

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
    private final Image img = getImage("http://s27.postimg.org/6qf9rmh8z/paint_miner_ess.png");
    // set variables
	private AntiBan anti_ban = new AntiBan();
	private Transportation transport = new Transportation();
	private Banker banker = new Banker();
	private GUI gui = new GUI();
    private boolean has_pic = false;
    private static final long startTime = System.currentTimeMillis();
    private int essence_mined = 0;
    private int current_ess = 0;
    private RSObject target_rock;
    private boolean execute = true;
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Font font = new Font("Verdana", Font.BOLD, 14);

    private final RSArea bank_area = new RSArea(new RSTile(3251, 3420, 0), new RSTile(3254, 3422, 0));
    private final RSArea teleport_area = new RSArea(new RSTile(3252, 3401, 0), new RSTile(3254, 3402, 0));
    private final RSTile exit_shop_tile = new RSTile(3253, 3498, 0);
    public boolean inventoryIsEmpty() {
    	if (Inventory.getAll().length > 0) {
    		return false;
    	}
    	
    	return true;
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
    	anti_ban.setHoverSkill(Skills.SKILLS.MINING);
    	
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
                    		if (portal[0].isOnScreen() && portal[0].isClickable()) {
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
                    			if (transport.validateWalk(portal[0].getPosition(), true)) {
                    				println("DOOMED");
                            		Camera.turnToTile(portal[0].getPosition());
                    				WebWalking.walkTo(portal[0].getPosition());
                    			}
                    		}
                    	}
                    	break;
                    case WALK_TO_ROCKS:
                    	log("Walking to rocks to mine ess");
                    	final RSObject[] rocks = Objects.findNearest(20, Filters.Objects.actionsContains("Mine"));
                    	RSNPC[] portals = NPCs.getAll();

                    	if (rocks.length > 0) {
                    		println("first one");
                    		Condition closeToRocks = new Condition() {
                                @Override
                                public boolean active() {
                                    General.sleep(100);
                                    return Objects.findNearest(7, Filters.Objects.actionsContains("Mine")).length > 0;
                                }
                            };
                            
                    		if (transport.walkCustomNavPath(portals[0].getPosition())) {
                    			General.sleep(1000);
                    			println("Won");
                    		} else {
                    			if (Walking.walkPath(transport.nav().findPath(20), closeToRocks, 500)) {
                    				println(" Found a win");
                    			} else {
                    				println("Another fail");
                    			}
                    		};
                    	} else {
                    		println("Second one");
                    		if (Walking.walkPath(transport.nav().findPath(5))) {
                    			General.sleep(1000);
                    			println("Win");
                    		} else {
                    			println("Fail");
                    		}
                    	}
                    	break;
                    case WALK_TO_BANK:
                    	log("Walking to bank");
                    	if (transport.walkCustomNavPath(bank_area.getRandomTile())) {
                    		println("Banking works");
                    		banker.handleBanking(bank_area, true);
                    	} else {
                    		println("Banking fails");
                    	}
                    	
//                    	if (Walking.walkPath(PathFinding.generatePath(Player.getPosition(), bank_area.getRandomTile(), false))) {
//                    		println("The path worked!");
//                    	};
//                    	banker.handleBanking(bank_area, true);
                        break;
                    case GET_PIC:
                    	log("Attempting to get a pickaxe");
                    	getPic();
                    	break;
                    case DEPOSIT_ITEMS:
                    	log("Depositing items in bank");
                        depositAll();
                        break;
                    case MINE_ROCKS:
                        mineRock(target_rock);
                        break;
                    case WALKING:
                    	anti_ban.handleWait();
                    	break;
                    case ANTI_BAN:
                    	anti_ban.handleWait();
                    	break;
                    case SOMETHING_WENT_WRONG:
                    	log("Stopping script, something went wrong");
                    	execute = false;
                    	break;
                }
            }
            // control cpu usage
            General.sleep(100,  250);
        }
    }

    private State state() {
        RSNPC[] portals = NPCs.getAll();
        // cache whether or not we have a pickaxe
        if (Inventory.find(gui.getPicaxeValue()).length > 0) {
        	has_pic = true;
        } else {
        	has_pic = false;
        }
        // determine how much ess we have mined
        if (Inventory.find("Rune Essence", "Pure Essence").length > 0) {
        	if (Inventory.getCount("Rune Essence", "Pure Essence") > current_ess) {
        		current_ess = Inventory.getCount("Rune Essence", "Pure Essence");
        		essence_mined ++;
        	};
        }
        // set state
        if (Inventory.isFull() && !Player.isMoving() && Objects.findNearest(20, "Rune Essence").length == 0) {
             if (Banking.isInBank() && Banking.isBankScreenOpen()) {
                 return State.DEPOSIT_ITEMS;
             } else if (!Banking.isBankScreenOpen()) {
                 return State.WALK_TO_BANK;
             }
        } else if (has_pic && !Inventory.isFull() && !Banking.isBankScreenOpen() && !Player.isMoving() && Objects.findNearest(20, "Rune Essence").length > 0 && Player.getAnimation() == -1) {
            RSObject[] nearest_rock = Objects.findNearest(9, "Rune Essence");
            

            if (nearest_rock.length > 0) {
            	
            	if (!nearest_rock[0].isOnScreen()) {
            		Camera.turnToTile(nearest_rock[0].getPosition());
            	}
            	target_rock = nearest_rock[0];
            	// anti ban compliance
                if (nearest_rock.length > 1 && this.anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.next() && transport.validateWalk(nearest_rock[1].getPosition(), true)) {
                    if (nearest_rock[1].getPosition().distanceToDouble(nearest_rock[0]) < 3.0)
                        target_rock = nearest_rock[1];
                }
                
                return State.MINE_ROCKS;
            }  else {
                return State.WALK_TO_ROCKS;
            }
        } else if (Player.isMoving()) {
        	return State.WALKING;
        } else if (has_pic && !Player.isMoving() && Objects.findNearest(20, "Rune Essence").length == 0) {
        	log("No essence in range");
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
                       General.sleep(100);
                       return Objects.find(20, "Rune Essence").length > 0; //until we are teleported to the mine
                   }
               }, General.random(5000, 8000));
           }
	   } else if (npc.length > 0 && !npc[0].isOnScreen()) {
		   Camera.turnToTile(npc[0].getPosition());
	   }
	   
	   return false;
   }
   
   private boolean canTeleport() {
		RSNPC[] npc = NPCs.find("Aubury");

		log("Searching for harry potter");
		if (npc.length > 0 && npc[0].getPosition().distanceToDouble(Player.getPosition()) > 5 && transport.validateWalk(npc[0].getPosition(), true)) {
			log("Walking to harry potters");
			return WebWalking.walkTo(npc[0].getPosition());
		} else if (npc.length > 0) {
			return true;
		} else if (transport.validateWalk(transport.getTile(teleport_area, false), false)) {
			WebWalking.walkTo(transport.getTile(teleport_area, true));
		}
		
		return false;

   }
   
    private boolean depositAll() {
    	if (Inventory.isFull()) {
    		if (banker.depositAllBut(gui.getPicaxeValue())) {
	        	// get pickaxe if needed
	        	if (Inventory.find(gui.getPicaxeValue()).length == 0) {
	        		getPic();
	        	}
	        	// condition to wait for getting the pick
	        	Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                    	// control cpu usage
                        General.sleep(100, 200);
                        // ensure we have deposited items
                        return Inventory.find(gui.getPicaxeValue()).length > 0;
                    }
                }, General.random(500, 1550));
	        	// update variables
	        	current_ess = 0;
	            log("Deposited items");
	            
	            // since we are walking around actually closing the screen isn't required
	            if (General.random(1, 5) >= 3) {
	            	closeBankScreen();
	            }
	            
	            return true;
	            
	        }
    	}
    	
    	return false;
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
   
    private void closeBankScreen() {
        if (Banking.isBankScreenOpen()) {
            Banking.close();
        }
    }

    private void mineRock(RSObject rock) {
    	if (!Inventory.isFull()) {
    		if (!rock.isOnScreen()) {
    			Camera.turnToTile(rock);
    		}
	        if (rock.isOnScreen() && rock.isClickable()) {
	        	if (DynamicClicking.clickRSObject(rock, "mine")) {
		        	Timing.waitCondition(new Condition() {
	                    @Override
	                    public boolean active() {
	                    	// control cpu usage
	                        General.sleep(100, 200);
	                        // ensure we have deposited items
	                        return Player.getAnimation() > -1 && !Player.isMoving();
	                    }
	                }, General.random(4000, 10000));
	        	}
	            anti_ban.abc.BOOL_TRACKER.HOVER_NEXT.reset();
	            anti_ban.abc.BOOL_TRACKER.USE_CLOSEST.reset();
	        }
    	}
    }

    private void log(String message) {
        println(message);
    }

    public void onPaint(Graphics g) {
        // setup image
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
    
    private Image getImage(String url) {
        // get paint image
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }
}