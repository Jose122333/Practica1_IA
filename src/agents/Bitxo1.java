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
        setVelocitatLineal(3);
        setVelocitatAngular(9);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
    {
       int esperaBusq=0;
       Punt puntoCercano;


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
                    espera=5;
                }
            } else {
                endavant();
                 if((esperaBusq > 0)){
                    esperaBusq--;
                } else {
                    puntoCercano=distanciaMinima();
                    if(!paredEnMedio(puntoCercano)){
                    mira(puntoCercano.x,puntoCercano.y);
                    esperaBusq=50;
                    }else {
                        esperaBusq=150;
                    }
                    //esperaBusq=50;
                    
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
    
       //Devuelve el punto con la distancia mínima en bonificacions, sin contar las minas
    Punt distanciaMinima(){
        int minimo;
        
        
        int [] distancias = new int [estat.bonificacions.length];
        
        //Copiar las distancias entre la posición actual y las bonificaciones
        for(int i=0;i<distancias.length;i++){
            if(estat.bonificacions[i].tipus!=5){
            distancias[i]=(int)estat.posicio.distancia(estat.bonificacions[i].posicio); 
            } else{
                distancias[i]= 1000000;
            }
        }
        minimo = devolverMinimo(distancias);
        return estat.bonificacions[minimo].posicio;      
    }
    //Devuelve el valor mínimo del array introducido
    int devolverMinimo(int[] distancias){
        int minimo;
        int indiceMin = 0;
        if(distancias.length>0){
            minimo = distancias[0];
           for(int i =0;i<distancias.length;i++){
                if(distancias[i]<minimo){
                    minimo = distancias[i];
                    indiceMin = i;
                }
            }
        }
        return indiceMin;
    }
       //Pared entre el bicho y el recurso
    boolean paredEnMedio(Punt p) {
        if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < estat.posicio.distancia(p)) {
            return true;
        } else if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < estat.posicio.distancia(p)) {
            return true;
        } else if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < estat.posicio.distancia(p)) {
            return true;
        } else {
            return false;
        }
    }
    
    
    //Hay que hacer una función que mire si hay una pared
    //entre el bicho y el recurso y si la hay descartarlo
    //y no añadirlo al array
    
    
}