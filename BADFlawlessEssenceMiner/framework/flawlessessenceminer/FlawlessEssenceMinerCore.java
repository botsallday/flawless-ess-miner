package scripts.BADFlawlessEssenceMiner.framework.flawlessessenceminer;

import org.tribot.script.Script;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.WebWalking;
import org.tribot.api.Timing;
import org.tribot.api.DynamicClicking;
import scripts.BADFlawlessEssenceMiner.api.antiban.BADAntiBan;
import scripts.BADFlawlessEssenceMiner.api.areas.BADAreas;
import scripts.BADFlawlessEssenceMiner.api.banking.BADBanking;
import scripts.BADFlawlessEssenceMiner.api.conditions.BADConditions;
import scripts.BADFlawlessEssenceMiner.api.transportation.BADTransportation;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Constants;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.types.RSItem;

public class FlawlessEssenceMinerCore extends Script {
    // set variables
    private BADAntiBan ANTI_BAN = new BADAntiBan();
    private static String ESSENCE = "Rune Essence";
    private BADTransportation TRANSPORT = new BADTransportation();
    private BADBanking BANKER = new BADBanking();
    private boolean has_pic = false;
    private int essence_mined = 0;
    private int current_ess = 0;
    private RSObject target_rock;
    private boolean execute = true;
    private String best_pickaxe;
    private boolean has_checked_for_best_pickaxe;
    final int[] PICKAXES = Constants.IDs.Items.pickaxes;
    
    public void run() {
        General.useAntiBanCompliance(true);
        ANTI_BAN.setHoverSkill(Skills.SKILLS.MINING);
        
        while(execute) {
            switch (state()) {
                case WALK_TO_TELEPORT:
                    General.println("Walking to rune shop for teleport");
                    if (canTeleport()) {
                        tryTeleport();
                    };
                    break;
                case WALK_TO_PORTAL:
                    RSNPC[] portal = NPCs.getAll();
                    
                    General.println("Walking to portal to leave ess mine");
                    if (portal.length > 0) {
                        if (portal[0].isOnScreen() && portal[0].isClickable()) {
                            if (DynamicClicking.clickRSNPC(portal[0], "Use")) {
                                Timing.waitCondition(BADConditions.objectIsNear(ESSENCE), General.random(4000, 8000));
                            } else if (DynamicClicking.clickRSNPC(portal[0], "Exit")) {
                                Timing.waitCondition(BADConditions.objectIsntNear(ESSENCE), General.random(4000, 8000));
                            }
                        } else {
                            if (TRANSPORT.validateWalk(portal[0].getPosition(), true)) {
                                Camera.turnToTile(portal[0].getPosition());
                                WebWalking.walkTo(portal[0].getPosition());
                            }
                        }
                    }
                    break;
                case WALK_TO_ROCKS:
                    General.println("Walking to rocks to mine ess");
                    final RSObject[] rocks = Objects.findNearest(20, Filters.Objects.actionsContains("Mine"));

                    if (rocks.length > 0) {
                        if (TRANSPORT.nav().traverse(rocks[0].getPosition().distanceTo(Player.getPosition()))) {
                            println(" Found a win");
                        } else {
                            println("Another fail");
                        }
                    } else {
                    	// if by chance we fail to walk to the rocks location, we will try to get in range of any rock
                    	if (TRANSPORT.nav().traverse(15)) {
                    		println("Random pathed");
                    	} else {
                    		println("Failed random path");
                    	}
                    }
                    break;
                case WALK_TO_BANK:
                    General.println("Walking to bank");
                    getToBank();
                    break;
                case GET_PIC:
                    General.println("Attempting to get a pickaxe");
                    getPic();
                    break;
                case DEPOSIT_ITEMS:
                    General.println("Depositing items in bank");
                    depositAll();
                    break;
                case MINE_ROCKS:
                    mineRock(target_rock);
                    break;
                case WALKING:
                    ANTI_BAN.handleWait();
                    break;
                case ANTI_BAN:
                    ANTI_BAN.handleWait();
                    break;
                case SOMETHING_WENT_WRONG:
                    General.println("Stopping script, something went wrong");
                    execute = false;
                    break;
            }
            // control cpu usage
            General.sleep(100,  250);
        }
    }

    @SuppressWarnings("deprecation")
	private State state() {
        RSNPC[] portals = NPCs.getAll();
        if (!has_checked_for_best_pickaxe && Banking.isInBank()) {
        	getBestPickaxe();
        } else if (!has_checked_for_best_pickaxe && isInMine() && !hasPickaxe()) {
        	// it won't leave the mine to check if it has a pic
        	return State.WALK_TO_PORTAL;
        } else if (!has_checked_for_best_pickaxe && !isInMine() && !Banking.isInBank()) {
        	return State.WALK_TO_BANK;
        }
        
        // cache whether or not we have a pickaxe
        has_pic = hasPickaxe();
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
                if (nearest_rock.length > 1 && this.ANTI_BAN.abc.BOOL_TRACKER.USE_CLOSEST.next() && TRANSPORT.validateWalk(nearest_rock[1].getPosition(), true)) {
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
            General.println("No essence in range");
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
   
   private boolean hasPickaxe(){
		return Equipment.find(PICKAXES).length==1||Inventory.find(PICKAXES).length==1;
	}
   
   private boolean tryTeleport() {
       RSNPC[] npc = NPCs.find("Aubury");

       if (npc.length > 0 && npc[0].getActions().length > 0 && npc[0].isOnScreen()) {
           RSNPC harry_potter = npc[0];
           General.println("Trying to get harry potter to teleport us");
           if (DynamicClicking.clickRSNPC(harry_potter, "Teleport")) {
               Timing.waitCondition(BADConditions.objectIsNear(ESSENCE), General.random(5000, 8000));
           }
       } else if (npc.length > 0 && !npc[0].isOnScreen()) {
           Camera.turnToTile(npc[0].getPosition());
       }
       
       return false;
   }
   
   public long getEssenceMined() {
	   return essence_mined;
   }
   
   private boolean canTeleport() {
        RSNPC[] npc = NPCs.find("Aubury");

        General.println("Searching for harry potter");
        if (npc.length > 0 && npc[0].getPosition().distanceToDouble(Player.getPosition()) > 5 && TRANSPORT.validateWalk(npc[0].getPosition(), true)) {
            General.println("Walking to harry potters");
            return WebWalking.walkTo(npc[0].getPosition());
        } else if (npc.length > 0) {
            return true;
        } else if (TRANSPORT.validateWalk(BADAreas.AUBURY_RUNE_SHOP_AREA.getRandomTile(), false)) {
            WebWalking.walkTo(BADAreas.AUBURY_RUNE_SHOP_AREA.getRandomTile());
        }
        
        return false;

   }
   
    private boolean depositAll() {
        if (Inventory.isFull()) {
            if (BANKER.depositAllBut(getPickaxeValue()) > 0) {
                // get pickaxe if needed
                if (Inventory.find(getPickaxeValue()).length == 0) {
                    getPic();
                }
                // condition to wait for getting the pick
                Timing.waitCondition(BADConditions.hasItem(getPickaxeValue()), General.random(500, 1550));
                // update variables
                current_ess = 0;
                General.println("Deposited items");
                
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
        RSItem[] pic = Banking.find(getPickaxeValue());
        
        if (Banking.openBank()) {
            if (pic.length > 0) {
                if (!Inventory.isFull() && Inventory.find(getPickaxeValue()).length == 0) {
                    Banking.withdraw(1, getPickaxeValue());
                } else {
                    depositAll();
                }
            }
        }
    }
    
    private String getPickaxeValue() {
    	return best_pickaxe;
    }
   
    private void closeBankScreen() {
        if (Banking.isBankScreenOpen()) {
            Banking.close();
        }
    }

    @SuppressWarnings("deprecation")
	private void mineRock(RSObject rock) {
        if (!Inventory.isFull()) {
            if (!rock.isOnScreen()) {
                Camera.turnToTile(rock);
            }
            if (rock.isOnScreen() && rock.isClickable() && DynamicClicking.clickRSObject(rock, "mine")) {
                Timing.waitCondition(BADConditions.WAIT_IDLE, General.random(4000, 10000));
                ANTI_BAN.abc.BOOL_TRACKER.HOVER_NEXT.reset();
                ANTI_BAN.abc.BOOL_TRACKER.USE_CLOSEST.reset();
            } else {
            	// if the large ess rock is not on screen or we have trouble clicking it, walk to it.
            	TRANSPORT.nav().traverse(rock.getPosition().distanceTo(Player.getPosition()));
            }
        }
    }

    public boolean isInMine() {
    	
    	RSObject[] ess = Objects.find(50, "Rune Essence");
    	
    	if (ess.length > 0) {
    		return true;
    	}
    	return false;
    }
    
    public void getBestPickaxe() {
    	
    	// ensure we are at the bank
    	if (Banking.openBank()) {
    		Timing.waitCondition(BADConditions.BANK_OPEN, 3000);
        	println("Getting best pickaxe");
        	int level = Skills.getActualLevel(Skills.SKILLS.MINING);
        	println("Mining level : "+level);
        	equipBestPickaxe(level);
        	has_checked_for_best_pickaxe = true;
    	}
    }
    
    public void equipBestPickaxe(int level) {
    	if (level >= 0) {
    		if (tryToFindPickaxe(bestUsablePickaxe(level))) {
    			// we're done
    			println("Found and equiped best pickaxe");
    		} else {
    			// we failed to get the best pic for our level, probably because we don't own it
    			println("Dropping level by 10 to find a different pic");
    			// as a last try attempt to get bronze pic
    			if (level - 10 < 0) {
    				equipBestPickaxe(0);
    			} else if (level == 0) {
    				execute = false;
    				return;
    			} else {
    				// try to get a lower level pickaxe if we dont own the best one based on our level
    				equipBestPickaxe(level - 10);
    			}
    		}
    	} else {
    		// we don't have a pickaxe that we can use!
    		execute = false;
    		println("No pickaxe that we meet the level requirements to use.");
    	}
    }
    
    public boolean tryToFindPickaxe(String pic) {
    	if (hasPickaxeInInventory(pic)) {
    		println("had the best pic in our inventory");
    		return equipPickaxe(pic);
    	}
    	
    	if (hasPickaxeEquiped(pic)) {
    		println("Had the best pic equiped");
    		return true;
    	}
    	
    	if (hasPickaxeInBank(pic)) {
    		println("had best pic in bank");
    		if (Banking.withdraw(1, pic+" pickaxe")) {
    			println("withdrew best pic from bank");
			       Timing.waitCondition(BADConditions.hasItem(pic), General.random(3000, 5000));
			       if (Banking.isBankScreenOpen()) {
			    	   Banking.close();
				       Timing.waitCondition(BADConditions.bankScreenIsClosed(), General.random(3000, 5000));
			       }
			       println("equip pic");
			       return equipPickaxe(pic);
    		};
    	}
    	
    	return false;
    }
    
    public boolean equipPickaxe(String pic) {
    	RSItem[] pickaxe = Inventory.find(Filters.Items.nameContains(pic+" pic"));
    	println("Attempting to equip pic");
    	println(pic);
    	if (pickaxe.length > 0) {
    		println("Found pic in inventory to equip");
    		if (pickaxe[0].click("Wield")) {
			       Timing.waitCondition(BADConditions.hasItemEquipped(pic+" pic"), General.random(3000, 5000));
			       best_pickaxe = pic;
			       return true;
    		}
    	}
    	
    	return false;
    }
    
    public boolean hasPickaxeInInventory(String pic) {
    	return Inventory.find(Filters.Items.nameContains(pic+" pic")).length > 0;
    }
    
    public boolean hasPickaxeInBank(String pic) {
    	if (Banking.isBankScreenOpen()) {
    		return Banking.find(Filters.Items.nameContains(pic+" pic")).length > 0;
    	}
    	
    	return false;
    }
    
    public boolean hasPickaxeEquiped(String pic) {
    	return Equipment.isEquipped(Filters.Items.nameContains(pic+" pic"));
    }
    
    public String bestUsablePickaxe(int level) {
	   
	   if (level > 60) {
		   return "Dragon";
	   } else if (level > 40) {
			return "Rune";
	   } else if (level > 30) {
			return "Adamant";
	   } else if (level > 20) {
			return "Mithril";
	   } else if (level > 10) {
		  return "Black";
	   } else if (level > 5) {
			return "Steel";
	   } else if (level > 0) {
			return "Iron";
	   }

	   return "Bronze";
    }
    
    public void getToBank() {
        if (TRANSPORT.walkCustomNavPath(BADAreas.VARROCK_EAST_BANK_AREA.getRandomTile())) {
            println("Banking works");
        } else {
            println("Banking fails");
            BANKER.walkToBank(BADAreas.VARROCK_EAST_BANK_AREA, true);
        }
        
        if (Banking.isInBank() && !Banking.isBankScreenOpen()) {
        	if (Banking.openBank() && Timing.waitCondition(BADConditions.BANK_OPEN, 3500)) {
        		println("Opened bank");
        	}
        }
    }
    
    public boolean inventoryIsEmpty() {
        if (Inventory.getAll().length > 0) {
            return false;
        }
        
        return true;
    }
}