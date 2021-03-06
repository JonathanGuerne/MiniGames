package ch.arc.ete;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;

import packets.BattleShip.BattleShipEndGamePacket;
import packets.BattleShip.BattleShipInGamePacket;
import packets.BattleShip.BattleShipStartConfirmPacket;
import packets.BattleShip.BattleShipStartInitGamePacket;
import packets.BattleShip.BattleShipStartPacket;
import packets.GamePlayerLeavingPacket;
import packets.Packet;

/* ---------------------------------------------------------------------------------------------
 * Projet        : HES d'été - Minis Games
 * Auteurs       : Marc Friedli, Anthony gilloz, Jonathan guerne
 * Date          : Juillet 2017
 * ---------------------------------------------------------------------------------------------
 * BattleShip.java   :  implementation of the battleship game
 * ---------------------------------------------------------------------------------------------
 */

public class BattleShip extends GameScreen {

    //constant to help understand what those value stand for
    private final int TAB_PLAYER = 0;
    private final int TAB_OPPONENT = 1;
    private final int TAB_TOUCHED = 2;
    private final int TAB_TOUCHED_OPPONENT = 3;
    private final int NB_CASE = 8;
    private final int NB_SHIP = 3;

    private ShapeRenderer shapeRenderer;
    private char charUser = 'x';
    private boolean showInit;
    private int shipInitialized;
    private boolean initGame;

    boolean inGame = false;
    boolean showMyTab = true;

    TextButton functionalButton;

    int touchIndex = -1;

    //array of image one for the player ships game boards and the other for the player shoot table
    private Sprite playerArrayImage[] = new Sprite[NB_CASE * NB_CASE];
    private Sprite opponentArrayImage[] = new Sprite[NB_CASE * NB_CASE];

    public BattleShip(Client client, Player localPlayer) {
        super(client, localPlayer);
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        w = Gdx.graphics.getWidth() / NB_CASE;
        h = gameLayoutHeight / NB_CASE;
        shapeRenderer.setColor(Color.BLACK);

        //setup 2 game boards for each player (one for ships and the other for shoot) see documentation
        tabGame = new HashMap<Integer, char[]>();
        tabGame.put(TAB_PLAYER, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_OPPONENT, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_TOUCHED, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_TOUCHED_OPPONENT, new char[NB_CASE * NB_CASE]);

        //tell the server that we wanna start a game
        BattleShipStartPacket bssp = new BattleShipStartPacket();
        bssp.idPlayer = localPlayer.getId();
        client.sendTCP(bssp);

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Packet) {
                    if (o instanceof BattleShipStartConfirmPacket) {
                        BattleShipStartConfirmPacket bsscp = (BattleShipStartConfirmPacket) o;

                        //A opponent is found
                        foundOpponent = true;
                        currentPlayerId = bsscp.idPlayer1;

                        //Find which player is the opponent from the packet
                        if (localPlayer.getId() == bsscp.idPlayer1) {
                            opponentPlayer = new Player(bsscp.idPlayer2, bsscp.namePlayer2);
                        } else {
                            opponentPlayer = new Player(bsscp.idPlayer1, bsscp.namePlayer1);
                        }
                        gameId = bsscp.gameId;

                        //The initialization phase is available
                        initGame = true;
                        showMessage = true;
                        shipInitialized = 0;
                    } else if (o instanceof BattleShipInGamePacket) {
                        BattleShipInGamePacket bsigp = (BattleShipInGamePacket) o;
                        currentPlayerId = bsigp.currentPlayerId;

                        //initialized the right board game if it's our turn or not
                        if (localPlayer.getId() == currentPlayerId) {
                            inGame = true;
                            tabGame.put(TAB_PLAYER, bsigp.currentPlayerTab);
                            tabGame.put(TAB_OPPONENT, bsigp.opponentPlayerTab);
                            tabGame.put(TAB_TOUCHED, bsigp.currentPlayerTabTouched);
                            tabGame.put(TAB_TOUCHED_OPPONENT, bsigp.opponentPlayerTabTouched);
                        } else {
                            tabGame.put(TAB_PLAYER, bsigp.opponentPlayerTab);
                            tabGame.put(TAB_OPPONENT, bsigp.currentPlayerTab);
                            tabGame.put(TAB_TOUCHED, bsigp.opponentPlayerTabTouched);
                            tabGame.put(TAB_TOUCHED_OPPONENT, bsigp.currentPlayerTabTouched);
                        }
                        gameId = bsigp.gameId;
                        displayCurrentPlayer(opponentPlayer.getPseudo());
                        showMessage = true;

                        for(Sprite s : playerArrayImage){
                            s = null;
                        }
                        for(Sprite s : opponentArrayImage){
                            s = null;
                        }


                        playerArrayImage = new Sprite[NB_CASE * NB_CASE];
                        opponentArrayImage = new Sprite[NB_CASE * NB_CASE];

                    } else if (o instanceof BattleShipEndGamePacket) {
                        //if the game is over
                        BattleShipEndGamePacket bsegp = (BattleShipEndGamePacket) o;


                        //show if the player win or loose from the packet
                        if (bsegp.idWinner == localPlayer.getId()) {
                            setCenterText("Vous avez gagnez");
                            showMessage = true;
                        } else {
                            setCenterText("Vous avez perdu!!");
                            showMessage = true;
                        }
                        gameOver = true;
                    }
                }
            }
        });
    }

    @Override
    public void display() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ApplicationSkin.getInstance().showBackground();

        //Show the right board game
        if (showMyTab) {
            batch.begin();
            for (int i = 0; i < playerArrayImage.length; i++) {
                if (playerArrayImage[i] != null) {
                    playerArrayImage[i].draw(batch);
                }
            }
            batch.end();
        } else {
            batch.begin();
            for (int i = 0; i < opponentArrayImage.length; i++) {
                if (opponentArrayImage[i] != null) {
                    opponentArrayImage[i].draw(batch);
                }
            }
            batch.end();
        }
        //draw the lines for the board game
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i <= NB_CASE; i++) {
            shapeRenderer.rectLine(i * w, 0, i * w, gameLayoutHeight - 3, 2);
            shapeRenderer.rectLine(0, i * h, Gdx.graphics.getWidth(), i * h, 2);
        }
        shapeRenderer.end();

        //If we need to show a message
        if (showMessage) {
            float x = 0;
            float y = Gdx.graphics.getHeight() / 2 + layout.height / 2;
            batch.begin();
            font.draw(batch, layout, x, y);
            batch.end();
        }
    }

    @Override
    public void update() {
        initImage(playerArrayImage, TAB_PLAYER, TAB_TOUCHED_OPPONENT, "pirate-ship");

        if (tabGame.get(TAB_TOUCHED).length != 0) {
            initImage(opponentArrayImage, TAB_TOUCHED, TAB_OPPONENT, "plouf");
        }

        if (inGame && touchIndex != -1) {
            //if a click is done from the board game
            BattleShipInGamePacket bsigp = new BattleShipInGamePacket();
            bsigp.currentPlayerId = localPlayer.getId();
            bsigp.opponentPlayerId = opponentPlayer.getId();
            bsigp.currentPlayerTab = tabGame.get(TAB_PLAYER);
            bsigp.currentPlayerTabTouched = tabGame.get(TAB_TOUCHED);
            bsigp.opponentPlayerTab = tabGame.get(TAB_OPPONENT);
            bsigp.opponentPlayerTabTouched = tabGame.get(TAB_TOUCHED_OPPONENT);
            bsigp.gameId = gameId;

            //send the packet with the new board touched game
            client.sendTCP(bsigp);
            inGame = false;
            touchIndex = -1;
        }
    }

    /**
     * method use to simplify image initialization
     * @param imageArray the array we want to put new image in
     * @param playerIndex the index of the game board to show (index of tabgame)
     * @param otherIndex the index which whom the playerIndex may have collision (player ships and opponent shoots)
     * @param spriteName the basic sprite to display
     */
    private void initImage(Sprite[] imageArray, int playerIndex, int otherIndex, String spriteName) {
        String spriteNameCopy = spriteName;
        for (int i = 0; i < imageArray.length; i++) {
            if (imageArray[i] == null && tabGame.get(playerIndex)[i] != '\0') {
                int x = i % NB_CASE;
                int y = NB_CASE - 1 - (i / NB_CASE);
                //if there is a collision the image is change to the one with the ship taking fire
                if (tabGame.get(playerIndex)[i] == tabGame.get(otherIndex)[i]) {
                    spriteName = "pirate-ship-dead";
                }

                Texture tex = new Texture(Gdx.files.internal("battleship/" + spriteName + ".png"));
                imageArray[i] = new Sprite(tex);
                imageArray[i].setX(x * w);
                imageArray[i].setY(y * h);
                imageArray[i].setSize(w, h);

                spriteName = spriteNameCopy;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        w = Gdx.graphics.getWidth() / NB_CASE;
        h = gameLayoutHeight / NB_CASE;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(gameOver)
        {
            //if the game is over a packet is sent to notify the server
            GamePlayerLeavingPacket gplp = new GamePlayerLeavingPacket();
            gplp.playerid = localPlayer.getId();
            client.sendTCP(gplp);
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, this.localPlayer));
        }
        else if (initGame && !showMessage) {
            //will place a ship on the case touch by the user
            if (screenY < informationLayoutHeight) {
                return false;
            }
            screenY -= informationLayoutHeight;
            int x = (int) (screenX / w);
            int y = (NB_CASE * (int) (screenY / h));
            touchIndex = x + y;
            //if the player already have a ship on this case it means that he wants to remove it
            if (tabGame.get(TAB_PLAYER)[touchIndex] == charUser) {
                tabGame.get(TAB_PLAYER)[touchIndex] = '\0';
                playerArrayImage[touchIndex] = null;
                shipInitialized--;
            } else if (shipInitialized < NB_SHIP) {
                tabGame.get(TAB_PLAYER)[touchIndex] = charUser;
                shipInitialized++;
            }

        } else if (inGame && !showMyTab) {
            //if we are ingame and we are diaplay the game board with the player's shoot
            if (screenY < informationLayoutHeight) {
                return false;
            }
            screenY -= informationLayoutHeight;
            int x = (int) (screenX / w);
            int y = (NB_CASE * (int) (screenY / h));

            touchIndex = x + y;
            if (tabGame.get(TAB_TOUCHED)[touchIndex] != charUser) {
                tabGame.get(TAB_TOUCHED)[touchIndex] = charUser;
            } else {
                touchIndex = -1;
            }
        }

        if (showMessage) {
            showMessage = false;
            setCenterText("");
        }
        return false;
    }



    @Override
    protected void setGameMenu() {

        //add element to the information part of the screen at the top

        if (initGame && !gameOver) {
            synchronized (stage) {
                setCenterText("Init de la partie");
            }
        }

        //this button will be use to confirm the placement of the ships at the beginning of the game
        //and then he will allow the player to switch between game board
        functionalButton = new TextButton("Confirmer", skin);

        functionalButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!initGame) {
                    //if we aren't in the initialization phase the board game is just change
                    showMyTab = !showMyTab;
                } else {
                    if (shipInitialized == NB_SHIP) {
                        //if there is the right number of ship on the board game we change the text value of the button
                        //and we finishe the initialization phase
                        functionalButton.setText("Inverser");

                        BattleShipStartInitGamePacket bssigp = new BattleShipStartInitGamePacket();
                        bssigp.idPlayer = localPlayer.getId();
                        bssigp.idOpponent = opponentPlayer.getId();
                        bssigp.tabGame = tabGame.get(TAB_PLAYER);
                        bssigp.gameId = gameId;

                        client.sendTCP(bssigp);
                        initGame = false;
                        touchIndex = -1;
                        setCenterText("Attente de l'autre joueur...");
                        showMessage = true;
                    } else {
                        //if there is missing ship
                        setCenterText("Il manque des bateaux");
                        showMessage = true;
                    }

                }
            }
        });

        tableDisplay.add(functionalButton);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
