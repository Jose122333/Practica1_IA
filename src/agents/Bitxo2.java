package agents;

// Exemple de Bitxo
import java.util.ArrayList;
import java.util.Arrays;

public class Bitxo2 extends Agent {

    static final int PARET = 0;
    static final int NAU = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;
    int espera = 0;
    int esperaBusqueda = 0;

    public Bitxo2(Agents pare) {
        super(pare, "Pikachu", "imatges/robotank2.gif");
    }

    @Override
    public void inicia() {
        setAngleVisors(30);
        setDistanciaVisors(400);
        setVelocitatLineal(5);
        setVelocitatAngular(2);
        espera = 0;
    }

    @Override
    public void avaluaComportament() {
        boolean enemic;

        enemic = false;

        int dir;

        activaEscut();
        estat = estatCombat();
        if (espera > 0) {
            espera--;
        } else {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara

                if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5) {
                    //dispara();   //bloqueig per nau, no giris dispara
                    perforadora();
                } else // hi ha un obstacle, gira i parteix
                {
                    gira(20); // 20 graus
                    if (hiHaParedDavant(20)) {
                        enrere();
                    } else {
                        endavant();
                    }
                    if (estat.enCollisio) {
                        esquerra();
                    }
                    espera = 3;
                }
            } else {

                endavant();

                if (estat.veigEnemic) {
                    if (estat.sector == 2 || estat.sector == 3) {
                        mira(estat.posicioEnemic.x, estat.posicioEnemic.y);
                    } else if (estat.sector == 1) {
                        dreta();
                    } else {
                        esquerra();
                    }
                }

                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant && estat.impactesRival < 5) {
                    // dispara();
                    if (estat.perforadores > 0) {
                        perforadora();
                    } else {
                        dispara();
                    }

                    activaEscut();
                    if (estat.balaEnemigaDetectada && estat.impactesRebuts == 4) {
                        hyperespai();
                    }
                }
                // Miram els visors per detectar els obstacles
                int sensor = 0;

                if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 45) {
                    sensor += 1;
                }
                if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 45) {
                    sensor += 2;
                }
                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 45) {
                    sensor += 4;
                }
                setVelocitatLineal(3);
                switch (sensor) {
                    case 0:
                        setVelocitatLineal(5);
                        setVelocitatAngular(2);
                        endavant();

                        if (esperaBusqueda > 0) {
                            esperaBusqueda--;
                        } else {
                            Bonificacio p;
                            p = distanciabonificacions();
                            if (estat.bonificacions.length < 2) {
                                mira(estat.posicioEnemic.x, estat.posicioEnemic.y);

                            } else if (p.tipus == MINA) {
                                mira(p.posicio.x, p.posicio.y);
                                dispara();
                                p = distanciabonificacions();
                                mira(p.posicio.x, p.posicio.y);

                            } else if (!hiHaParedDavant(1)) {
                                if (calcDistancia(p.posicio.x, p.posicio.y) < 150) {
                                    mira(p.posicio.x, p.posicio.y);
                                }
                            }
                        }

                        break;
                    case 1:  // esquerra bloquejada
                        espera = 5;
                        esperaBusqueda = 15;
                        setVelocitatAngular(5);
                        dreta();

                        break;
                    case 3:  // esquerra  i centro bloquejada
                        esperaBusqueda = 15;
                        espera = 5;
                        setVelocitatAngular(5);
                        dreta();

                        break;
                    case 4: // dreta bloquejada
                        esperaBusqueda = 15;
                        espera = 5;
                        setVelocitatAngular(5);
                        esquerra();

                        break;
                    case 6:  // dreta i centro  bloquejada
                        esperaBusqueda = 15;
                        espera = 5;
                        setVelocitatAngular(5);
                        esquerra();

                        break;

                    case 5: //esquerra i dreta bloquetjat
                        esperaBusqueda = 15;
                        setVelocitatLineal(5);
                        setVelocitatAngular(2);
                        endavant();
                        break;  // centre lliure
                    case 2:  // paret devant
                        esperaBusqueda = 15;
                        espera = 5;
                        setVelocitatAngular(5);
                        esquerra();
                        if (estat.enCollisio) {
                            dreta();
                        }

                    case 7:  // si estic molt aprop, --> torna enrere esuqerra dreta i centro bloquetjat
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 3) {
                            espera = 8;
                            // enrere();
                            gira(180);
                        } else // gira aleatòriament a la dreta o a l'esquerra
                        //                        if (distancia < 50) {
                        //                            if (Math.random() * 500 < 250) {
                        //                                dreta();
                        //                            } else {
                        {
                            esquerra();
                        }
//                            }
//                        }
                        break;
                }

            }
        }
    }

    boolean hiHaParedDavant(int dist) {

        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <= dist) {
            return true;
        }

        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] <= dist) {
            return true;
        }

        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] <= dist) {
            return true;
        }

        return false;
    }

    double minimaDistanciaVisors() {
        double minim;

        minim = Double.POSITIVE_INFINITY;
        if (estat.objecteVisor[ESQUERRA] == PARET) {
            minim = estat.distanciaVisors[ESQUERRA];
        }
        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < minim) {
            minim = estat.distanciaVisors[CENTRAL];
        }
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < minim) {
            minim = estat.distanciaVisors[DRETA];
        }
        return minim;
    }

    Bonificacio distanciabonificacions() {
        int[] distancia = new int[estat.bonificacions.length];
        // Punt p = new Punt();
        int dx;
        int dy;
        int dist;
        int mx = estat.posicio.x;
        int my = estat.posicio.y;
        int menor;
        int distan;

        for (int i = 0; i < estat.bonificacions.length; i++) {

            distancia[i] = calcDistancia(estat.bonificacions[i].posicio.x, estat.bonificacions[i].posicio.y);

        }

        menor = 0;
        distan = distancia[0];
        for (int i = 1; i < distancia.length; i++) {
            if (distancia[i] <= distan) {
                menor = i;
                distan = distancia[i];
            }
        }
        //p.x = estat.bonificacions[menor].posicio.x;
        //p.y = estat.bonificacions[menor].posicio.y;

        return estat.bonificacions[menor];
    }

    int calcDistancia(int x, int y
    ) {
        int mx = estat.posicio.x;
        int my = estat.posicio.y;
        int dx;
        int dy;
        dx = x - mx; //punto final menos el inicial 
        dy = y - my;
        double re = (double) dx * dx + dy * dy;
        int i = (int) Math.sqrt(re);
        return i;
    }

}
