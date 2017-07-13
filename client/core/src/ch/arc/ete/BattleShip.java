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

/**
 * Created by jonathan.guerne on 01.05.2017.
 */

public class BattleShip extends GameScreen {

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

        tabGame = new HashMap<Integer, char[]>();
        tabGame.put(TAB_PLAYER, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_OPPONENT, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_TOUCHED, new char[NB_CASE * NB_CASE]);
        tabGame.put(TAB_TOUCHED_OPPONENT, new char[NB_CASE * NB_CASE]);

        BattleShipStartPacket bssp = new BattleShipStartPacket();
        bssp.idPlayer = localPlayer.getId();

        client.sendTCP(bssp);

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object o) {
                if (o instanceof Packet) {
                    if (o instanceof BattleShipStartConfirmPacket) {
                        BattleShipStartConfirmPacket bsscp = (BattleShipStartConfirmPacket) o;
                        foundOpponent = true;
                        currentPlayerId = bsscp.idPlayer1;

                        if (localPlayer.getId() == bsscp.idPlayer1) {
                            opponentPlayer = new Player(bsscp.idPlayer2, bsscp.namePlayer2);
                        } else {
                            opponentPlayer = new Player(bsscp.idPlayer1, bsscp.namePlayer1);
                        }
                        System.out.println("oposant " + bsscp.idPlayer2 + " " + bsscp.idPlayer1);
                        gameId = bsscp.gameId;

                        initGame = true;
                        showMessage = true;
                        shipInitialized = 0;
                        System.out.println("J'ai recu un packet de confirm");
                    } else if (o instanceof BattleShipInGamePacket) {
                        String text;
                        BattleShipInGamePacket bsigp = (BattleShipInGamePacket) o;
                        currentPlayerId = bsigp.currentPlayerId;
                        if (localPlayer.getId() == currentPlayerId) {
                            text = "C'est votre tour";
                            inGame = true;
                            tabGame.put(TAB_PLAYER, bsigp.currentPlayerTab);
                            tabGame.put(TAB_OPPONENT, bsigp.opponentPlayerTab);
                            tabGame.put(TAB_TOUCHED, bsigp.currentPlayerTabTouched);
                            tabGame.put(TAB_TOUCHED_OPPONENT, bsigp.opponentPlayerTabTouched);
                        } else {
                            text = "C'est le tour de votre adversaire";
                            tabGame.put(TAB_PLAYER, bsigp.opponentPlayerTab);
                            tabGame.put(TAB_OPPONENT, bsigp.currentPlayerTab);
                            tabGame.put(TAB_TOUCHED, bsigp.opponentPlayerTabTouched);
                            tabGame.put(TAB_TOUCHED_OPPONENT, bsigp.currentPlayerTabTouched);
                        }
                        gameId = bsigp.gameId;
                        setCenterText(text);
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
                        BattleShipEndGamePacket bsegp = (BattleShipEndGamePacket) o;
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

        if (initGame) {
            synchronized (stage) {
                setCenterText("Init de la partie");
            }
        }

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
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i <= NB_CASE; i++) {
            shapeRenderer.line(i * w, 0, i * w, gameLayoutHeight - 3);
            shapeRenderer.line(0, i * h, Gdx.graphics.getWidth(), i * h);
        }
        shapeRenderer.end();
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
            System.out.println("J'ai jouer!!");
            BattleShipInGamePacket bsigp = new BattleShipInGamePacket();
            bsigp.currentPlayerId = localPlayer.getId();
            bsigp.opponentPlayerId = opponentPlayer.getId();
            bsigp.currentPlayerTab = tabGame.get(TAB_PLAYER);
            bsigp.currentPlayerTabTouched = tabGame.get(TAB_TOUCHED);
            bsigp.opponentPlayerTab = tabGame.get(TAB_OPPONENT);
            bsigp.opponentPlayerTabTouched = tabGame.get(TAB_TOUCHED_OPPONENT);
            bsigp.gameId = gameId;

            client.sendTCP(bsigp);
            inGame = false;
            touchIndex = -1;
        }
    }

    private void initImage(Sprite[] imageArray, int playerIndex, int otherIndex, String spriteName) {
        String spriteNameCopy = spriteName;
        for (int i = 0; i < imageArray.length; i++) {
            if (imageArray[i] == null && tabGame.get(playerIndex)[i] != '\0') {
                int x = i % NB_CASE;
                int y = NB_CASE - 1 - (i / NB_CASE);
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

        if (initGame && !showMessage ) {

            if (screenY < informationLayoutHeight) {
                return false;
            }
            screenY -= informationLayoutHeight;
            int x = (int) (screenX / w);
            int y = (NB_CASE * (int) (screenY / h));
            touchIndex = x + y;
            if (tabGame.get(TAB_PLAYER)[touchIndex] == charUser) {
                tabGame.get(TAB_PLAYER)[touchIndex] = '\0';
                playerArrayImage[touchIndex] = null;
                shipInitialized--;
            } else if (shipInitialized < NB_SHIP) {
                tabGame.get(TAB_PLAYER)[touchIndex] = charUser;
                shipInitialized++;
            }
        } else if (gameOver) {
            GamePlayerLeavingPacket gplp = new GamePlayerLeavingPacket();
            gplp.playerid = localPlayer.getId();
            client.sendTCP(gplp);
            ((Game) Gdx.app.getApplicationListener()).setScreen(new MainMenu(client, this.localPlayer));
        } else if (inGame && !showMyTab) {
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
        functionalButton = new TextButton("Confirmer", skin);

        functionalButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (!initGame) {
                    showMyTab = !showMyTab;
                } else {

                    if (shipInitialized == NB_SHIP) {
                        functionalButton.setText("Inverser");

                        BattleShipStartInitGamePacket bssigp = new BattleShipStartInitGamePacket();
                        bssigp.idPlayer = localPlayer.getId();
                        System.out.println("opponent " + opponentPlayer.getId());
                        bssigp.idOpponent = opponentPlayer.getId();
                        bssigp.tabGame = tabGame.get(TAB_PLAYER);
                        bssigp.gameId = gameId;

                        client.sendTCP(bssigp);
                        initGame = false;
                        touchIndex = -1;
                        setCenterText("Attente de l'autre joueur...");
                        showMessage = true;


                    } else {
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
