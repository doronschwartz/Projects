package edu.yu.cs.intro.doomGame;
import edu.yu.cs.intro.TestHelper;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Assignment9Tests {

    class IncorrectBehaviorException extends RuntimeException{
        public IncorrectBehaviorException(String message){
            super(message);
        }
    };

    private TestHelper helper;
    private String output;
    private int pointsEarned;
    private int possiblePoints;
    protected static final String PASSED = "+++++TEST PASSED+++++\n\n";
    protected static final String FAILED = "+++++TEST FAILED+++++\n\n";

    public static void main(String[] args) throws IOException {
        Assignment9Tests a8t = new Assignment9Tests();
        a8t.runTests();
        writeResultsToFile(a8t.pointsEarned,a8t.possiblePoints,a8t.output);
    }

    protected static void writeResultsToFile(int pointsEarned, int possiblePoints, String output) throws IOException{
        //write points file
        File pointsFile = new File("grade.txt");
        pointsFile.createNewFile();
        PrintWriter writer = new PrintWriter(pointsFile);
        writer.println(pointsEarned);
        writer.flush();
        writer.close();

        //write details file
        File details = new File("results.txt");
        details.createNewFile();
        writer = new PrintWriter(details);
        writer.println("YOUR SCORE: " + pointsEarned + " out of " + possiblePoints + "\n");
        writer.println(output);
        writer.flush();
        writer.close();
    }
    public Assignment9Tests() {
        this.helper = new TestHelper();
        this.helper.setCharDifLimit(12);
        this.output = "";
        this.pointsEarned = 0;
        this.possiblePoints = 0;
    }
    private void runTest(String name, String description) {
        this.runTest(12,name,description);
    }

    private void runTest(int points, String name, String description){
        try {
            this.output += "TEST NAME: " + name + "\n";
            this.output += "TEST DESCRIPTION: " + description + "\n";
            this.output += "TEST POINT VALUE: " + points + "\n";
            this.possiblePoints += points;
            this.helper.runMethod(Assignment9Tests.class,this,name);
            this.output += PASSED;
            this.pointsEarned += points;
        }
        catch (Throwable e) {
            this.output += this.helper.getExceptionOutput(e.getCause());
            this.output += FAILED;
        }
    }
    public void runTests() {
        //tests on Monster
        this.runTest("testAttackWithInsufficientWeapon", "Monster: does monster.attack throw an IllegalArgumentException when attacked with an insufficient weapon?");
        this.runTest("testAttackWithNoAmmo", "Monster: does monster.attack throw an IllegalArgumentException when attacked with no ammunition?");
        this.runTest("testAttackDeadMonster", "Monster: does Monster.attack throw an IllegalStateException when the monster was attacked when it was already dead?");
        this.runTest("testAttackMonsterButDontKillMonster", "Monster: Monster.isDead should return false after being attacked with less than ammunitionCountNeededToKill");
        this.runTest("testKillMonsterInTwoPasses", "Monster: Monster attacked with ammunitionCountNeededToKill over the course of 2 calls on monster.attack should be dead, i.e. should return true from isDead()");
        this.runTest("testGetDefaultProtector", "Monster: Monster with no custom protector should return the protector of its monster type from Monster.getProtectedBy()");
        this.runTest("testGetCustomProtector", "Monster: Monster with custom protector should return the custom protector from Monster.getProtectedBy()");
        this.runTest("testMonsterCompareTo", "Monster: test Monster.compareTo");

        //tests on player
        this.runTest("testPlayerDeath", "Player: Does Player.isDead return true when health is == 0?");
        this.runTest("testPlayerAlive", "Player: Does Player.isDead return false when health is == 100?");
        this.runTest("testChangePlayerHealth", "Player: Does Player.changeHealth result in health changing as expected?");
        this.runTest("testReducePlayerHealth", "Player: Does Player.changeHealth, with a negative number, result in health changing as expected?");
        this.runTest("testAddAmmo", "Player: Does Player.addAmmunition result in ammunition count changing as expected?");
        this.runTest("testChangeAmmo", "Player: Does Player.changeAmmunitionRoundsForWeapon result in ammunition count changing as expected?");
        this.runTest("testReduceAmmo", "Player: Does Player.changeAmmunitionRoundsForWeapon result in ammunition count beign reduced as expected?");
        this.runTest("testHasWeapon", "Player: Does Player.hasWeapon return what is expected based on what weapons we have given him?");
        this.runTest("testPlayerCompareTo", "Player: Does Player.compareTo return what is expected?");

        //tests on Room
        this.runTest("testRoomDangerLevel", "Room: Danger level should equal ordinal+1 values of all monsters combined");
        this.runTest("testRoomCompareTo", "Room: compareTo should compare based on danger level");
        this.runTest("testGetWeaponsWonUponCompletion", "Room: did not get expected set of weapons won upon completion");
        this.runTest("testGetWeaponsWonUponCompletion", "Room: check keys and values of room.getAmmoWonUponCompletion()");

        //tests on GameBot
        this.runTest(36,"testSinglePlayerSuccessGame", "GameBot: Play Single Player Success");
        this.runTest(36,"testSinglePlayerFailureGame", "GameBot: Play Single Player Failure");
        this.runTest(36,"testMultiPlayerSuccessGame", "GameBot: Play Multi-Player Success");
        this.runTest(36,"testMultiPlayerFailureGame", "GameBot: Play Multi-Player Failure");
        this.runTest(36,"testReapCompletionRewards", "GameBot: Check that player reaps the rewards of completing a room: GameBot.reapCompletionRewards()");
        this.runTest(36,"testKillMonster", "GameBot: Check that killMonster kills the right set of monsters and results in the correct changes to the player");
        this.runTest("testGetCompletedRooms", "GameBot: Check that Gamebot.getCompletedRooms() returns all completed rooms, and no other rooms");
        this.runTest("testGetLivePlayers", "GameBot: Check that Gamebot.getLivePlayers() returns all live players, and no other players");
        this.runTest("testGetLivePlayersWithWeaponAndAmmunition", "GameBot: Check that Gamebot.getLivePlayersWithWeaponAndAmmunition() returns the correct set of players");
        this.runTest(24, "testGetAllProtectorsInRoom", "GameBot: Check that Gamebot.getAllProtectorsInRoom() returns the correct set of monsters");
        this.runTest(36, "testCanKill", "GameBot: Check that Gamebot.canKill() responds correctly");

    }
    //***************test the GameBot class***************

    public void testCanKill(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> group = new TreeSet<>();
        group.add(baron);
        group.add(imp);
        group.add(spectre);
        group.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room fearsome = new Room(group,weapons,ammoWonUponCompletion,0,"Fearsome Foursome");
        Player p = new Player("test dummy", 100);
        if(!GameBot.canKill(p,imp,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned false for a healthy player being able to kill an imp");
        }
        p = new Player("test dummy", 0);
        if(GameBot.canKill(p,imp,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned true for a dead player being able to kill an imp");
        }
        p = new Player("test dummy", 100);
        p.addWeapon(Weapon.CHAINSAW);
        if(GameBot.canKill(p,demon,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned true for a player with the chainsaw and 5 rounds of ammo being able to kill the demon even through he doesn't have the shotgun to kill his protector, the baron of hell");
        }
        p.addWeapon(Weapon.SHOTGUN);
        p.addAmmunition(Weapon.SHOTGUN,7);
        if(GameBot.canKill(p,demon,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned true for a player being able to kill a demon even though he doesn't have the pistol to kill the protector's protector, i.e. spectre" );
        }
        p.addWeapon(Weapon.PISTOL);
        if(GameBot.canKill(p,demon,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned true for a player being able to kill a demon even though he doesn't have enough pistol ammo to kill the protector's protector, i.e. spectre" );
        }
        p.addAmmunition(Weapon.PISTOL,1);
        if(!GameBot.canKill(p,demon,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned false for a player being able to kill a demon even though he should be able to");
        }
        p.setHealth(10);
        if(GameBot.canKill(p,demon,fearsome)){
            throw new IncorrectBehaviorException("GameBot.canKill returned true for a player being able to kill a demon even though he will run out of health before killing the demon");
        }
    }


    public void testGetAllProtectorsInRoom(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> group = new TreeSet<>();
        group.add(baron);
        group.add(imp);
        group.add(spectre);
        group.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room fearsome = new Room(group,weapons,ammoWonUponCompletion,0,"Fearsome Foursome");
        SortedSet<Monster> protectors = GameBot.getAllProtectorsInRoom(imp,fearsome);
        if(protectors.size() != 0){
            throw new IncorrectBehaviorException("GameBot.getAllProtectorsInRoom returned at least one protector for a monster that had none");
        }
        protectors = GameBot.getAllProtectorsInRoom(demon,fearsome);
        if(protectors.size() != 2 || !protectors.contains(spectre) || !protectors.contains(baron)){
            throw new IncorrectBehaviorException("GameBot.getAllProtectorsInRoom did not return the two protectors of the demon");
        }
        protectors = GameBot.getAllProtectorsInRoom(baron,fearsome);
        if(protectors.size() != 1 || !protectors.contains(spectre)){
            throw new IncorrectBehaviorException("GameBot.getAllProtectorsInRoom did not return the one protector of the baron of hell");
        }
    }


    public void testGetLivePlayersWithWeaponAndAmmunition(){
        Player p1 = new Player("p1", 100);
        p1.addWeapon(Weapon.PISTOL);
        p1.addAmmunition(Weapon.PISTOL,12);
        p1.addWeapon(Weapon.SHOTGUN);
        p1.addAmmunition(Weapon.SHOTGUN,12);
        Player p2 = new Player("p2", 100);
        p2.addWeapon(Weapon.PISTOL);
        p2.addAmmunition(Weapon.PISTOL,15);
        Player p3 = new Player("p3", 100);
        p3.addWeapon(Weapon.SHOTGUN);
        p3.addAmmunition(Weapon.SHOTGUN,18);
        Player p4 = new Player("p4", 100);
        p4.addWeapon(Weapon.CHAINSAW);
        p4.addAmmunition(Weapon.CHAINSAW,12);
        TreeSet<Player> players = new TreeSet<>();
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(createEasyImpsRoom());
        GameBot bot = new GameBot(rooms,players);
        //one player out of two with the given weapon has enough ammo
        Set<Player> armed = bot.getLivePlayersWithWeaponAndAmmunition(Weapon.PISTOL,19);
        if(armed.size() != 1 || !armed.contains(p2)){
            throw new IncorrectBehaviorException("bot.getLivePlayersWithWeaponAndAmmunition did not return the expected player (p2)");
        }
        //two players with weapon and enough ammo
        armed = bot.getLivePlayersWithWeaponAndAmmunition(Weapon.PISTOL,17);
        if(armed.size() != 2 || !armed.contains(p2) || !armed.contains(p1) ){
            throw new IncorrectBehaviorException("bot.getLivePlayersWithWeaponAndAmmunition did not return the expected players (p1,p2)");
        }
        //one player with the weapon & ammo
        armed = bot.getLivePlayersWithWeaponAndAmmunition(Weapon.CHAINSAW,17);
        if(armed.size() != 1 || !armed.contains(p4)){
            throw new IncorrectBehaviorException("bot.getLivePlayersWithWeaponAndAmmunition did not return the expected players (p4)");
        }
        //NO player with the weapon & ammo
        armed = bot.getLivePlayersWithWeaponAndAmmunition(Weapon.SHOTGUN,100);
        if(armed.size() != 0 ){
            throw new IncorrectBehaviorException("bot.getLivePlayersWithWeaponAndAmmunition did not return the expected players (none!)");
        }
        //ALL players with the weapon & ammo
        armed = bot.getLivePlayersWithWeaponAndAmmunition(Weapon.FIST,1000);
        if(armed.size() != 4 ){
            throw new IncorrectBehaviorException("bot.getLivePlayersWithWeaponAndAmmunition did not return the expected players (all players)");
        }
    }

    public void testGetLivePlayers(){
        Player p1 = new Player("p1", 100);
        Player p2 = new Player("p2", 101);
        Player p3 = new Player("p3", 102);
        Player p4 = new Player("p4", 103);
        TreeSet<Player> players = new TreeSet<>();
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(createEasyImpsRoom());
        GameBot bot = new GameBot(rooms,players);
        p2.setHealth(0);
        p4.setHealth(0);
        Set<Player> live = bot.getLivePlayers();
        if(live.size()!= 2 || !live.contains(p1) || !live.contains(p3)){
            throw new IncorrectBehaviorException("A Player which was alive was missing in the set returned by GameBot.getLivePlayers() or there was the wrong number elements in the set");
        }
    }

    public void testGetCompletedRooms(){
        GameBot bot = createSinglePlayerFailure();
        bot.play();

        Set<Room> rooms = bot.getCompletedRooms();
        if(rooms.size() != 2 ||
                findRoomByName(rooms,"Easy Imps") == null ||
                findRoomByName(rooms,"Double Demonic") == null){
            throw new IncorrectBehaviorException("A Room which was completed was missing in the set returned by GameBot.getCompletedRooms() or there was the wrong number elements in the set");
        }
    }
    private Room findRoomByName(Set<Room> rooms, String name){
        for(Room room : rooms){
            if(room.getName().equals(name)){
                return room;
            }
        }
        return null;
    }


    public void testKillMonster(){
        //setup the room

        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> monsters = new TreeSet<>();
        monsters.add(baron);
        monsters.add(imp);
        monsters.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"test room");
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(room);
        //setup the player
        TreeSet<Player> players = new TreeSet<>();
        int initialhealth = 100;
        Player p = new Player("test man",initialhealth);
        p.addWeapon(demon.getMonsterType().weaponNeededToKill);
        if(demon.getMonsterType().ammunitionCountNeededToKill > 5){
            p.addAmmunition(demon.getMonsterType().weaponNeededToKill,demon.getMonsterType().ammunitionCountNeededToKill - 5);
        }
        p.addWeapon(baron.getMonsterType().weaponNeededToKill);
        if(baron.getMonsterType().ammunitionCountNeededToKill > 5){
            p.addAmmunition(baron.getMonsterType().weaponNeededToKill,baron.getMonsterType().ammunitionCountNeededToKill - 5);
        }
        players.add(p);
        //test the gameBot's changes to a player after monsters are killed
        GameBot g = new GameBot(rooms,players);
        g.killMonster(p,room,demon);
        //check that monsters are dead
        if(!baron.isDead()){
            throw new IncorrectBehaviorException("after killMonster was called on demon, baronOfHell should be dead as well");
        }
        if(!demon.isDead()){
            throw new IncorrectBehaviorException("after killMonster was called on demon, demon should be dead");
        }
        //check that ammunition has been reduced
        if(p.getAmmunitionRoundsForWeapon(baron.getMonsterType().weaponNeededToKill) != 0){
            throw new IncorrectBehaviorException("killing the baron of hell should've used up all the ammunition for the weapon needed to kill it");
        }
        if(p.getAmmunitionRoundsForWeapon(demon.getMonsterType().weaponNeededToKill) != 4){
            throw new IncorrectBehaviorException("killing the demon should've used up one round of chainsaw ammo, leaving 4");
        }
        //check that health has been reduced
        int expectedReduction = (demon.getMonsterType().playerHealthLostPerExposure * 2) + baron.getMonsterType().playerHealthLostPerExposure + (imp.getMonsterType().playerHealthLostPerExposure * 2);
        if(p.getHealth() != initialhealth - expectedReduction){
            throw new IncorrectBehaviorException("Player health was nto reduced by expected amount when killing a monster");
        }

    }

    public void testReapCompletionRewards(){
        TreeSet<Monster> monsters = new TreeSet<>();
        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.PISTOL);
        weapons.add(Weapon.SHOTGUN);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.PISTOL,10);
        ammoWonUponCompletion.put(Weapon.CHAINSAW,10);
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"test room");
        Player p = new Player("test man",10);
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(room);
        TreeSet<Player> players = new TreeSet<>();
        players.add(p);
        GameBot g = new GameBot(rooms,players);
        g.reapCompletionRewards(p,room);
        if(!p.hasWeapon(Weapon.PISTOL)  || p.getAmmunitionRoundsForWeapon(Weapon.PISTOL)!= 15){
            throw new IncorrectBehaviorException("Player should have the pistol and 15 rounds of ammunition");
        }
        if(!p.hasWeapon(Weapon.SHOTGUN)  || p.getAmmunitionRoundsForWeapon(Weapon.SHOTGUN)!= 5){
            throw new IncorrectBehaviorException("Player should have the shotgun and 5 rounds of ammunition");
        }
        if(p.getAmmunitionRoundsForWeapon(Weapon.CHAINSAW)!= 10){
            throw new IncorrectBehaviorException("Player should have 5 rounds of ammunition for the shotgun");
        }
        if(p.getHealth()!= 30){
            throw new IncorrectBehaviorException("Player should have 30 health units");
        }
    }


    public void testMultiPlayerFailureGame(){
        String gameName = "Multi-Player Failure";
        boolean success = playAndReport(createMultiPlayerFailure(),"Multi-Player Failure");
        if(success){
            throw new IncorrectBehaviorException("The test game " + gameName + " succeeded, although it was expected to fail");
        }
    }
    public void testMultiPlayerSuccessGame(){
        String gameName = "Multi-Player Success";
        boolean success = playAndReport(createMultiPlayerSuccess(),"Multi-Player Success");
        if(!success){
            throw new IncorrectBehaviorException("The test game " + gameName + " failed, although it was expected to succeed");
        }
    }

    public void testSinglePlayerFailureGame(){
        String gameName = "Single Player Failure";
        boolean success = playAndReport(createSinglePlayerFailure(),"Single Player Failure");
        if(success){
            throw new IncorrectBehaviorException("The test game " + gameName + " succeeded, although it was expected to fail");
        }
    }

    public void testSinglePlayerSuccessGame(){
        String gameName = "Single Player Success";
        boolean success = playAndReport(createSinglePlayerSuccess(),gameName);
        if(!success){
            throw new IncorrectBehaviorException("The test game " + gameName + " failed, although it was expected to succeed");
        }
    }

    //***************test the Room class***************

    public void testGetPlayerHealthLostPerEncounter(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> monsters = new TreeSet<>();
        monsters.add(baron);
        monsters.add(imp);
        monsters.add(spectre);
        monsters.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"Fearsome Foursome");
        int healthLost = 0;
        for(Monster m : monsters){
            healthLost += m.getMonsterType().playerHealthLostPerExposure;
        }
        if(healthLost != room.getPlayerHealthLostPerEncounter()){
            throw new IncorrectBehaviorException("room.getPlayerHealthLostPerEncounter() should equal the sum of playerHealthLostPerExposure values of all live monsters combined. Expected: " + healthLost + ". Actual: " + room.getPlayerHealthLostPerEncounter());
        }
    }

    public void testResultsOfMonsterDeath(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> monsters = new TreeSet<>();
        monsters.add(baron);
        monsters.add(imp);
        monsters.add(spectre);
        monsters.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"Fearsome Foursome");

        baron.attack(baron.getMonsterType().weaponNeededToKill,baron.getMonsterType().ammunitionCountNeededToKill);
        room.monsterKilled(baron);
        if(room.isCompleted()){
            throw new IncorrectBehaviorException("Room is not considered completed until all monsters are dead");
        }
        if(room.getMonsters().size() != 4){
            throw new IncorrectBehaviorException("size of room.getMonsters() is not the expected size");
        }
        if(room.getLiveMonsters().size() != 3){
            throw new IncorrectBehaviorException("size of room.getLiveMonsters() is not the expected size");
        }
        if(room.getDeadMonsters().size() != 1){
            throw new IncorrectBehaviorException("size of room.getDeadMonsters() is not the expected size");
        }
    }

    public void testGetAmmoWonUponCompletion(){
        TreeSet<Monster> monsters = new TreeSet<>();
        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.CHAINSAW);
        weapons.add(Weapon.PISTOL);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"Fearsome Foursome");
        if(!room.getWeaponsWonUponCompletion().contains(Weapon.CHAINSAW) || !room.getWeaponsWonUponCompletion().contains(Weapon.PISTOL) || room.getWeaponsWonUponCompletion().size() != 2){
            throw new IncorrectBehaviorException("did not get expected set of weapons won upon completion");
        }
    }

    public void testGetWeaponsWonUponCompletion(){
        TreeSet<Monster> monsters = new TreeSet<>();
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.PISTOL,5);
        ammoWonUponCompletion.put(Weapon.CHAINSAW,10);
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"Fearsome Foursome");
        if(!room.getAmmoWonUponCompletion().keySet().contains(Weapon.PISTOL) || !room.getAmmoWonUponCompletion().keySet().contains(Weapon.CHAINSAW)){
            throw new IncorrectBehaviorException("An expected key is missing from the set returned by room.getAmmoWonUponCompletion()");
        }
        if(room.getAmmoWonUponCompletion().get(Weapon.PISTOL)!=5 || room.getAmmoWonUponCompletion().get(Weapon.CHAINSAW)!=10){
            throw new IncorrectBehaviorException("An expected value is wrong in the set returned by room.getAmmoWonUponCompletion()");
        }
    }

    public void testRoomCompareTo(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster imp2 = new Monster(MonsterType.IMP);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> monsters = new TreeSet<>();
        monsters.add(imp);
        monsters.add(imp2);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        Room room1 = new Room(monsters,weapons,ammoWonUponCompletion,20,"room1");
        TreeSet<Monster> monsters2 = new TreeSet<>();
        monsters2.add(baron);
        Room room2 = new Room(monsters2,weapons,ammoWonUponCompletion,20,"room2");
        if(room1.compareTo(room2) != -1){
            throw new IncorrectBehaviorException("compareTo should compare based on danger level; expected -1, actual return value: " + room1.compareTo(room2));
        }
        if(room2.compareTo(room1) != 1){
            throw new IncorrectBehaviorException("compareTo should compare based on danger level; expected 1, actual return value: " + room2.compareTo(room1));
        }
    }

    public void testRoomDangerLevel(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> monsters = new TreeSet<>();
        monsters.add(baron);
        monsters.add(imp);
        monsters.add(spectre);
        monsters.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.CHAINSAW);
        weapons.add(Weapon.PISTOL);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.PISTOL,5);
        ammoWonUponCompletion.put(Weapon.CHAINSAW,10);
        Room room = new Room(monsters,weapons,ammoWonUponCompletion,20,"Fearsome Foursome");
        int danger = 0;
        for(Monster m : monsters){
            danger += m.getMonsterType().ordinal() + 1;
        }
        if(danger != room.getDangerLevel()){
            throw new IncorrectBehaviorException("Danger level should equal ordinal+1 values of all live monsters combined. Expected: " + danger + ". Actual: " + room.getDangerLevel());
        }
        baron.attack(baron.getMonsterType().weaponNeededToKill,baron.getMonsterType().ammunitionCountNeededToKill);
        room.monsterKilled(baron);
        spectre.attack(spectre.getMonsterType().weaponNeededToKill,spectre.getMonsterType().ammunitionCountNeededToKill);
        room.monsterKilled(spectre);
        danger -= (baron.getMonsterType().ordinal()+1 + spectre.getMonsterType().ordinal() +1);
        int roomLevel = room.getDangerLevel();
        if(danger != roomLevel){
            throw new IncorrectBehaviorException("Danger level should equal ordinal+1 values of all live monsters combined. Expected: " + danger + ". Actual: " + roomLevel);
        }
    }

    //***************test the Player class***************

    public void testHasWeapon() {
        //test has weapon
        Player p = new Player("test man",100);
        p.addWeapon(Weapon.CHAINSAW);
        if(!(p.hasWeapon(Weapon.CHAINSAW) && p.hasWeapon(Weapon.FIST))){
            throw new IncorrectBehaviorException("Player.hasWeapon is returning false for a weapon that he should have (either FIRST or one that was added)");
        }
        if( p.hasWeapon(Weapon.PISTOL) || p.hasWeapon(Weapon.SHOTGUN)){
            throw new IncorrectBehaviorException("Player.hasWeapon is returning true for a weapon that he should NOT have");
        }
    }

    public void testPlayerCompareTo(){
        //test compareTo - same weapons and ammo
        Player p = new Player("test man",100);
        Player p2 = new Player("test man2",100);
        if(p.compareTo(p2) != 0){
            throw new IncorrectBehaviorException("Player.compareTo is not returning 0 for equivalent players");
        }

        //p has greater weapon
        p = new Player("test man",100);
        p2 = new Player("test man2",100);
        p.addWeapon(Weapon.PISTOL);
        p2.addWeapon(Weapon.CHAINSAW);
        if(p.compareTo(p2) != 1){
            throw new IncorrectBehaviorException("Player.compareTo is not returning 1 when the player on which it is being called has a greater weapon");
        }

        //same weapon, p2 has more ammo
        p = new Player("test man",100);
        p2 = new Player("test man2",100);
        p.addWeapon(Weapon.PISTOL);
        p2.addWeapon(Weapon.PISTOL);
        p2.addAmmunition(Weapon.PISTOL,10);
        if(p.compareTo(p2) != -1){
            throw new IncorrectBehaviorException("Player.compareTo is not returning -1 when the player on which it is being called has less ammunition");
        }

        //same weapon and ammo, p has more health
        p = new Player("test man",110);
        p2 = new Player("test man2",100);
        p.addWeapon(Weapon.PISTOL);
        p2.addWeapon(Weapon.PISTOL);
        if(p.compareTo(p2) != 1){
            throw new IncorrectBehaviorException("Player.compareTo is not returning 1 when the player on which it is being called has more health");
        }
    }
    public void testChangeAmmo() {
        //test change ammo, positive
        Player p = new Player("test man",100);
        p.addWeapon(Weapon.CHAINSAW);
        p.changeAmmunitionRoundsForWeapon(Weapon.CHAINSAW,10);
        if(p.getAmmunitionRoundsForWeapon(Weapon.CHAINSAW) != 15){
            throw new IncorrectBehaviorException("Player.changeAmmunitionRoundsForWeapon did not result in ammunition count changing as expected");
        }
    }
    public void testReduceAmmo() {
        //test change ammo, negative
        Player p = new Player("test man",100);
        p.addWeapon(Weapon.CHAINSAW);
        p.changeAmmunitionRoundsForWeapon(Weapon.CHAINSAW,-3);
        if(p.getAmmunitionRoundsForWeapon(Weapon.CHAINSAW) != 2){
            throw new IncorrectBehaviorException("Player.changeAmmunitionRoundsForWeapon did not result in ammunition count beign reduced as expected");
        }
    }

    public void testPlayerDeath() {
        //test player death
        Player p = new Player("test man", 100);
        p.setHealth(0);
        if(!p.isDead()){
            throw new IncorrectBehaviorException("Player.isDead did not return true when health was == 0");
        }
    }
    public void testPlayerAlive() {
        //test set health, non-zero
        Player p = new Player("test man", 100);
        if(p.isDead()){
            throw new IncorrectBehaviorException("Player.isDead did not return false when health was == 100");
        }
    }
    public void testChangePlayerHealth() {
        //test change player health, positive
        Player p = new Player("test man",100);
        p.changeHealth(50);
        if(p.getHealth() != 150){
            throw new IncorrectBehaviorException("Player.changeHealth did not result in health changing as expected");
        }
    }
    public void testReducePlayerHealth() {
        //test change player health, positive
        Player p = new Player("test man",100);
        p.changeHealth(-50);
        if(p.getHealth() != 50){
            throw new IncorrectBehaviorException("Player.changeHealth, with a negative number, did not result in health changing as expected");
        }
    }
    public void testAddAmmo() {
        //test add ammunition followed by adding the weapon for which the ammo is for
        Player p = new Player("test man",100);
        p.addAmmunition(Weapon.PISTOL,10);
        if(p.getAmmunitionRoundsForWeapon(Weapon.PISTOL)!=10){
            throw new IncorrectBehaviorException("Player.addAmmunition did not result in ammunition count changing as expected");
        }
    }

    //***************test the Monster class***************
    public void testMonsterCompareTo(){
        Monster demon = new Monster(MonsterType.DEMON);
        Monster imp = new Monster(MonsterType.IMP);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        Monster imp2 = new Monster(MonsterType.IMP);
        int compare = demon.compareTo(demon);
        if(compare!=0){
            throw new IncorrectBehaviorException("Monster.compareTo should return 0 when compared to itself, but returned " + compare);
        }
        compare = demon.compareTo(baron);
        if(compare!=1){
            throw new IncorrectBehaviorException("Monster.compareTo should return 1 when compared to its protector, but instead returned " + compare);
        }
        compare = baron.compareTo(demon);
        if(compare!=-1){
            throw new IncorrectBehaviorException("Monster.compareTo should return -1 when compared to a monster it protects, but instead returned " + compare);
        }
        compare = spectre.compareTo(imp);
        if(compare!=1){
            throw new IncorrectBehaviorException("Monster.compareTo should return 1 when compared to a monster with a lower ordinal, but instead returned " + compare);
        }
        compare = imp.compareTo(demon);
        if(compare!=-1){
            throw new IncorrectBehaviorException("Monster.compareTo should return -1 when compared to a monster with a higher ordinal, but instead returned " + compare);
        }
        //compare two monsters of the same type, which will be determined by hashCode
        int shouldReturn = 0;
        if(imp.hashCode() < imp2.hashCode()){
            shouldReturn = -1;
        }else if(imp.hashCode() > imp2.hashCode()){
            shouldReturn = 1;
        }
        compare = imp.compareTo(imp2);
        if(compare!=shouldReturn){
            throw new IncorrectBehaviorException("Monster.compareTo should be based on hashCode when comparing two monsters of the same type. Should've returned " + shouldReturn + " but instead returned " + compare);
        }
        shouldReturn*=-1;
        compare = imp2.compareTo(imp);
        if(compare!=shouldReturn){
            throw new IncorrectBehaviorException("(2) Monster.compareTo should be based on hashCode when comparing two monsters of the same type. Should've returned " + shouldReturn + " but instead returned " + compare);
        }
    }
    public void testGetCustomProtector(){
        Monster m = new Monster(MonsterType.DEMON,MonsterType.IMP);
        if(m.getProtectedBy() != MonsterType.IMP){
            throw new IncorrectBehaviorException("Monster with custom protector did not return the custom protector");
        }
    }
    public void testGetDefaultProtector(){
        Monster m = new Monster(MonsterType.DEMON);
        if(m.getProtectedBy() != m.getMonsterType().getProtectedBy()){
            throw new IncorrectBehaviorException("Monster with no custom protector did not return the protector of its monster type");
        }
    }
    private Room getBlankRoom(){
        return new Room(new TreeSet<Monster>(),new HashSet<Weapon>(),new HashMap<Weapon,Integer>(),0,"room");
    }
    public void testKillMonsterInTwoPasses(){
        Monster m = new Monster(MonsterType.SPECTRE);
        m.setRoom(getBlankRoom());
        try{
            m.attack(m.getMonsterType().weaponNeededToKill,m.getMonsterType().ammunitionCountNeededToKill-1);
            m.attack(m.getMonsterType().weaponNeededToKill,1);
            if(!m.isDead()){
                throw new IncorrectBehaviorException("Monster should be dead but isDead returned false");
            }
        }catch(IllegalStateException e){}
    }
    public void testAttackWithInsufficientWeapon(){
        Monster m = new Monster(MonsterType.SPECTRE);
        try{
            m.attack(Weapon.CHAINSAW,m.getMonsterType().ammunitionCountNeededToKill);
            throw new IncorrectBehaviorException("When attacked with a CHAINSAW, a SPECTRE should throw an IllegalArgumentException");
        }catch(IllegalArgumentException e){}
    }
    public void testAttackWithNoAmmo(){
        Monster m = new Monster(MonsterType.BARON_OF_HELL);
        try{
            m.attack(m.getMonsterType().weaponNeededToKill,0);
            throw new IncorrectBehaviorException("When attacked with a insufficient ammunition, a BARON_OF_HELL should throw an IllegalArgumentException");
        }catch(IllegalArgumentException e){}
    }
    public void testAttackDeadMonster(){
        Monster m = new Monster(MonsterType.DEMON);
        m.setRoom(getBlankRoom());
        try{
            m.attack(m.getMonsterType().weaponNeededToKill,m.getMonsterType().ammunitionCountNeededToKill);
            m.attack(m.getMonsterType().weaponNeededToKill,1);
            throw new IncorrectBehaviorException("Monster.attack did not throw an IllegalStateException when the monster was attacked when it was already dead");
        }catch(IllegalStateException e){}
    }
    public void testAttackMonsterButDontKillMonster(){
        Monster m = new Monster(MonsterType.SPECTRE);
        try{
            m.attack(m.getMonsterType().weaponNeededToKill,m.getMonsterType().ammunitionCountNeededToKill-1);
            if(m.isDead()){
                throw new IncorrectBehaviorException("Monster should not be dead but isDead returned true");
            }
        }catch(IllegalStateException e){}
    }

    //***********************************************************************************************************
    //******************************Methods for creating GameBot Scenarios***************************************
    //***********************************************************************************************************
    private boolean playAndReport(GameBot bot, String gameName){
        System.out.println("Test Game named " + gameName + " now being played");
        boolean success = bot.play();
        boolean complete = true;
        for(Room room : bot.getAllRooms()){
            System.out.println("**********************************************");
            System.out.println("Room name: " + room.getName());
            System.out.println("Room completed: " + room.isCompleted());
            if(!room.isCompleted()){
                complete = false;
            }
            System.out.println("State of Monsters in Room:");
            for(Monster m : room.getMonsters()){
                System.out.println(m.getMonsterType().name() + ": is dead - " + m.isDead());
                if(!m.isDead()){
                    complete = false;
                }
            }
        }
        return success && complete;
    }

    public GameBot createMultiPlayerFailure(){
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(this.createEasyImpsRoom());
        rooms.add(this.createDoubleDemonicRoom());
        rooms.add(this.createFearsomeFoursomeRoom());
        Player player1 = new Player("Player 1",5);
        Player player2 = new Player("Player 2",10);
        player2.addWeapon(Weapon.CHAINSAW);
        player2.addWeapon(Weapon.PISTOL);
        player2.addWeapon(Weapon.SHOTGUN);
        player2.addAmmunition(Weapon.CHAINSAW,1);
        player2.addAmmunition(Weapon.PISTOL,6);

        TreeSet<Player> players = new TreeSet<>();
        players.add(player1);
        players.add(player2);
        return new GameBot(rooms,players);
    }


    public GameBot createSinglePlayerFailure(){
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(this.createEasyImpsRoom());
        rooms.add(this.createDoubleDemonicRoom());
        rooms.add(this.createFearsomeFoursomeRoom());
        Player player1 = new Player("Player 1",100);
        TreeSet<Player> players = new TreeSet<>();
        players.add(player1);
        return new GameBot(rooms,players);
    }


    public GameBot createMultiPlayerSuccess(){
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(this.createEasyImpsRoom());
        rooms.add(this.createDoubleDemonicRoom());
        rooms.add(this.createFearsomeFoursomeRoom());
        Player player1 = new Player("Player 1",9);
        Player player2 = new Player("Player 2",100);
        player2.addWeapon(Weapon.CHAINSAW);
        player2.addWeapon(Weapon.PISTOL);
        player2.addWeapon(Weapon.SHOTGUN);
        player2.addAmmunition(Weapon.CHAINSAW,2);
        player2.addAmmunition(Weapon.PISTOL,7);
        player2.addAmmunition(Weapon.SHOTGUN,13);

        TreeSet<Player> players = new TreeSet<>();
        players.add(player1);
        players.add(player2);
        return new GameBot(rooms,players);
    }

    public GameBot createSinglePlayerSuccess(){
        TreeSet<Room> rooms = new TreeSet<>();
        rooms.add(this.createEasyImpsRoom());
        rooms.add(this.createDoubleDemonicRoom());
        rooms.add(this.createIDRoom());
        rooms.add(this.createTripleThreatRoom());
        rooms.add(this.createDemonQuadRoom());
        rooms.add(this.createFearsomeFoursomeRoom());
        Player player1 = new Player("Player 1",100);
        TreeSet<Player> players = new TreeSet<>();
        players.add(player1);
        return new GameBot(rooms,players);
    }

    private Room createEasyImpsRoom(){
        Monster imp1 = new Monster(MonsterType.IMP);
        Monster imp2 = new Monster(MonsterType.IMP);
        Monster imp3 = new Monster(MonsterType.IMP);
        TreeSet<Monster> group1 = new TreeSet<>();
        group1.add(imp1);
        group1.add(imp2);
        group1.add(imp3);

        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.CHAINSAW);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.CHAINSAW,2);
        return new Room(group1,weapons,ammoWonUponCompletion,10,"Easy Imps");
    }

    private Room createDoubleDemonicRoom(){
        Monster demon1 = new Monster(MonsterType.DEMON);
        Monster demon2 = new Monster(MonsterType.DEMON);
        TreeSet<Monster> group1 = new TreeSet<>();
        group1.add(demon1);
        group1.add(demon2);

        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.PISTOL);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.PISTOL,6);
        ammoWonUponCompletion.put(Weapon.CHAINSAW,2);
        return new Room(group1,weapons,ammoWonUponCompletion,0,"Double Demonic");
    }

    private Room createIDRoom(){
        Monster imp1 = new Monster(MonsterType.IMP);
        Monster imp2 = new Monster(MonsterType.IMP);
        Monster imp3 = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        TreeSet<Monster> group1 = new TreeSet<>();
        group1.add(imp1);
        group1.add(imp2);
        group1.add(imp3);
        group1.add(demon);

        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.CHAINSAW);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.PISTOL,6);
        return new Room(group1,weapons,ammoWonUponCompletion,0,"I&D");
    }

    private Room createTripleThreatRoom(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        TreeSet<Monster> group = new TreeSet<>();
        group.add(imp);
        group.add(demon);
        group.add(spectre);

        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.SHOTGUN);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.PISTOL,6);
        ammoWonUponCompletion.put(Weapon.SHOTGUN,4);
        ammoWonUponCompletion.put(Weapon.CHAINSAW,6);
        return new Room(group,weapons,ammoWonUponCompletion,0,"Triple Threat");
    }

    private Room createDemonQuadRoom(){
        Monster demon = new Monster(MonsterType.DEMON);
        Monster demon1 = new Monster(MonsterType.DEMON);
        Monster demon2 = new Monster(MonsterType.DEMON);
        Monster demon3 = new Monster(MonsterType.DEMON);
        TreeSet<Monster> group = new TreeSet<>();
        group.add(demon);
        group.add(demon1);
        group.add(demon2);
        group.add(demon3);

        HashSet<Weapon> weapons = new HashSet<>();
        weapons.add(Weapon.SHOTGUN);
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        ammoWonUponCompletion.put(Weapon.SHOTGUN,16);
        ammoWonUponCompletion.put(Weapon.CHAINSAW,1);
        return new Room(group,weapons,ammoWonUponCompletion,0,"Demon Quad");
    }


    private Room createFearsomeFoursomeRoom(){
        Monster imp = new Monster(MonsterType.IMP);
        Monster demon = new Monster(MonsterType.DEMON);
        Monster spectre = new Monster(MonsterType.SPECTRE);
        Monster baron = new Monster(MonsterType.BARON_OF_HELL);
        TreeSet<Monster> group = new TreeSet<>();
        group.add(baron);
        group.add(imp);
        group.add(spectre);
        group.add(demon);
        HashSet<Weapon> weapons = new HashSet<>();
        Map<Weapon,Integer> ammoWonUponCompletion = new HashMap<>();
        return new Room(group,weapons,ammoWonUponCompletion,0,"Fearsome Foursome");
    }
}
