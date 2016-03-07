package agents;

// Exemple de Bitxo
import java.util.Arrays;
import java.util.Comparator;
public class Bitxo1 extends Agent {

    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;

    Estat estat;
    int espera =0;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1", "imatges/robotank1.gif");
    }
  
    @Override
    public void inicia()
    {
        
        setAngleVisors(10);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(6);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
    {
        boolean enemic;
        int puntosRojos;
            
        Punt[] posicionesRojos;
        double[] distancias;

        enemic = false;

        int dir;

        estat = estatCombat();
        
        if (espera > 0) {
            espera--;
        }
        else
        {
            atura();

            if (estat.enCollisio) // situació de nau bloquejada
            {
                // si veu la nau, dispara

                if (estat.objecteVisor[CENTRAL] == NAU && estat.impactesRival < 5)
                {
                    dispara();   //bloqueig per nau, no giris dispara
                }
                else // hi ha un obstacle, gira i parteix
                {
                    gira(20); // 20 graus
                    if (hiHaParedDavant(20)) enrere();
                    else endavant();
                    espera=3;
                }
            } else {
                endavant();
                //ESTO YA ES NUESTRO
                if(estat.nbonificacions > 0){   
                    puntosRojos = quedanPuntos();
                    if(puntosRojos>0){
                        posicionesRojos = new Punt[puntosRojos];
                        distancias = new double[puntosRojos];
                        posicionesRojos = obtenerPuntos(posicionesRojos);
                        distancias = calcularDistancias(posicionesRojos);
                        mira(estat.posicio.x-estat.bonificacions[0].posicio.x,estat.posicio.y-estat.bonificacions[0].posicio.y);
                        endavant();
                    }
                }
//                if (estat.veigEnemic)
//                {
//                    if (estat.sector == 2 || estat.sector == 3)
//                        mira(estat.posicioEnemic.x, estat.posicioEnemic.y);
//                    else if (estat.sector == 1)  dreta();
//                    else  esquerra();
//                }

                if (estat.objecteVisor[CENTRAL] == NAU && !estat.disparant && estat.impactesRival < 5)
                {
                    dispara();
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

                switch (sensor) {
                    case 0:
                        endavant();
                        break;
                    case 1:
                    case 3:  // esquerra bloquejada
                        dreta();
                        break;
                    case 4:
                    case 6:  // dreta bloquejada
                        esquerra();
                        break;
                    case 5:
                        endavant();
                        break;  // centre lliure
                    case 2:  // paret devant
                    case 7:  // si estic molt aprop, torna enrere
                        double distancia;
                        distancia = minimaDistanciaVisors();

                        if (distancia < 15) {
                            espera = 8;
                            enrere();
                        } else // gira aleatòriament a la dreta o a l'esquerra
//                        if (distancia < 50) {
//                            if (Math.random() * 500 < 250) {
//                                dreta();
//                            } else {
                                esquerra();
//                            }
//                        }
                        break;
                }

            }
        }
    }

    boolean hiHaParedDavant(int dist)
    {

       if (estat.objecteVisor[ESQUERRA]== PARET && estat.distanciaVisors[ESQUERRA]<=dist)
           return true;

       if (estat.objecteVisor[CENTRAL ]== PARET && estat.distanciaVisors[CENTRAL ]<=dist)
           return true;

       if (estat.objecteVisor[DRETA   ]== PARET && estat.distanciaVisors[DRETA   ]<=dist)
           return true;

       return false;
    }

    double minimaDistanciaVisors()
    {
        double minim;

        minim = Double.POSITIVE_INFINITY;
        if (estat.objecteVisor[ESQUERRA] == PARET)
            minim = estat.distanciaVisors[ESQUERRA];
        if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL]<minim)
            minim = estat.distanciaVisors[CENTRAL];
        if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA]<minim)
            minim = estat.distanciaVisors[DRETA];
        return minim;
    }
    
    int quedanPuntos(){
     int i = 0;
     int ret = 0;
     while(i< estat.nbonificacions){
         if(estat.bonificacions[i].tipus==Agent.PUNTS){
            ret++;
            
         }i++;
     }
     return ret;
     
    }
    
    Punt[] obtenerPuntos(Punt[] posicionesRojos){
        
     int i = 0;
     while(i< estat.nbonificacions){
         if(estat.bonificacions[i].tipus==Agent.PUNTS){
            posicionesRojos[i]=estat.bonificacions[i].posicio;
         }i++;
     }
     return posicionesRojos;        
    }
    
    
    
    double[] calcularDistancias(Punt[] posicionesRojos){
        double[] puntos= new double[posicionesRojos.length];
        for(int i = 0; i<posicionesRojos.length; i++){
           puntos[i]=posicionesRojos[i].distancia(estat.posicio);
        }
        return puntos;
    }
    
    int distanciaManhattanX(){
        return Math.abs(estat.posicio.x-estat.bonificacions[0].posicio.x);
    }
    
    
    int distanciaManhattanY(){
        return Math.abs(estat.posicio.y-estat.bonificacions[0].posicio.y);
    }

}