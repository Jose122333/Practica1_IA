package agents;

// Exemple de Bitxo,
public class Bitxo2 extends Agent {

    static final int PARET = 0;
    static final int NAU = 1;
    static final int RES = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL = 1;
    static final int DRETA = 2;

    Estat estat;
    int espera = 0;
    int esperaBusq = 0;
    int vida = 0;

    public Bitxo2(Agents pare) {
        super(pare, "Sonic", "imatges/soni.gif");
    }

    @Override
    public void inicia() {
        setAngleVisors(10);
        setDistanciaVisors(400);
        setVelocitatLineal(4);
        setVelocitatAngular(3);
        estat = estatCombat();
        espera = 0;
        esperaBusq = 0;
    }

    @Override
    public void avaluaComportament() {
        vida = estat.impactesRebuts;
        estat = estatCombat();
        Bonificacio bonificacionCercana;
        
                    //Mirar si vamos ganando
            if (estat.temps == 5000 && estat.punts > estat.puntsRival && estat.hyperespaiDisponibles > 0 && !estat.hiperEspaiActiu) {
                hyperespai();
            }

        if (espera > 0) {
            espera--;
        } else {
            atura();


            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara
                if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5) {
                    dispara();   //bloqueig per nau, no giris dispara
                } else // hi ha un obstacle, gira i parteix
                {
                    gira(20);
                    if (hiHaParedDavant(30)) {
                        enrere();
                    } else {
                        endavant();
                    }
                    espera = 3;
                }
            } else {

                setVelocitatAngular(3);

                //Pasar modo ataque
                if (estat.veigEnemic) {

                    if (estat.sector == 2 || estat.sector == 3) {
                        mira(estat.posicioEnemic.x, estat.posicioEnemic.y);
                    }

                    if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5) {
                        if (estat.perforadores > 0) {
                            perforadora();
                        } else {
                            dispara();
                        }

                    }

                    if (estat.balaEnemigaDetectada && estat.impactesRebuts >= 3 && estat.hyperespaiDisponibles > 0) {
                        hyperespai();
                    }
                    if (estat.balaEnemigaDetectada && estat.impactesRebuts < 3) {
                        activaEscut();
                    }

                    // si no ve el enemigo 
                } else{ //Si te disparan por la espalda
                if (vida < estat.impactesRebuts) {
                    gira(180);
                }
                }
                // Miram els visors per detectar els obstacles
                int sensor = 0;

                if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 30) {
                    sensor += 1;
                }
                if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 30) {
                    sensor += 2;
                }
                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 30) {
                    sensor += 4;
                }
                switch (sensor) {
                    case 0:
                        endavant();
                        break;
                    case 1:
                    case 3:  // esquerra bloquejada
                        setVelocitatAngular(9);
                        dreta();
                        break;
                    case 4:
                    case 6:  // dreta bloquejada
                        setVelocitatAngular(9);
                        esquerra();
                        break;
                    case 5:
                        endavant();
                        break;  // centre lliure
                    case 2:  // paret devant
                    case 7:  // si estic molt aprop, torna enrere
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 10) {
                            enrere();
                            espera = 8;
                        } else if(distancia < 50){
                          //Girar izquierda o derecha para no perder tiempo según los grados
                         if (estat.angle >= 0 || estat.angle <= 180) {
                                setVelocitatAngular(9);
                                gira(45);
                                endavant();
                            } else {

                                setVelocitatAngular(9);
                                gira(-45);
                                endavant();
                            }
                        }
                        break;
                }
                if ((esperaBusq > 0)) {
                    esperaBusq--;
                } else {
                    endavant();
                    bonificacionCercana = distanciaMinima();
                    if (!hiHaParedDavant((int) estat.posicio.distancia(bonificacionCercana.posicio))) {
                        mira(bonificacionCercana.posicio.x, bonificacionCercana.posicio.y);
                        if (bonificacionCercana.tipus == 5) {
                            dispara();
                        }
                    } else {
                        esperaBusq = 8;
                    }
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

    //Devuelve la bonificación con la distancia mínima en bonificacions, sin contar las minas
    Bonificacio distanciaMinima() {
        int minimo;

        int[] distancias = new int[estat.bonificacions.length];

        //Copiar las distancias entre la posición actual y las bonificaciones
        for (int i = 0; i < distancias.length; i++) {
            distancias[i] = (int) estat.posicio.distancia(estat.bonificacions[i].posicio);

        }
        minimo = devolverMinimo(distancias);
        return estat.bonificacions[minimo];
    }

    //Devuelve el valor mínimo del array introducido
    int devolverMinimo(int[] distancias) {
        int minimo;
        int indiceMin = 0;
        if (distancias.length > 0) {
            minimo = distancias[0];
            for (int i = 0; i < distancias.length; i++) {
                if (distancias[i] < minimo) {
                    minimo = distancias[i];
                    indiceMin = i;
                }
            }
        }
        return indiceMin;
    }
}
