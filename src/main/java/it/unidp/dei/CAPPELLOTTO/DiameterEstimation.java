package it.unidp.dei.CAPPELLOTTO;

import it.unidp.dei.Point;

import java.util.ArrayList;
import java.util.TreeMap;


//E' QUELLO CON K = 1, quindi al massimo ha 2 punti in AV
public class DiameterEstimation
{
    public DiameterEstimation(double _eps)
    {
        eps = _eps;
        //MAPPE DI INTEGER, POINT
        cold = new TreeMap<>();
        cnew = new TreeMap<>();
        q = new TreeMap<>();
        r = new TreeMap<>();
    }

    public double query()
    {
        //Se non ho nulla in CNEW, cioè non ho punti, ritorno 0
        if(cnew.isEmpty()) {
            return 0;
        }

        //Altrimenti, ritorno (1+eps)^(i-1) per il primo i presente in cnew che non ha punti associati (il suo value è null)
        for(int i=cnew.firstKey(); i<=cnew.lastKey(); i++){
            if(cnew.get(i) == null) {
                return Math.pow(1 + eps, i - 1); // r < OPT < 3*r*(1+eps)
            }
        }

        //Se hanno tutti un punto associato, ritorno -1
        return -1;
    }

    public void update(Point p, int time)
    {
        //Se non c'è nessun pt2 e nessuno pt1 si mettono (pt2 e' il piu' vecchio)
        if(pt2 == null){
            pt2 = p;
            return;
        }
		if(pt1 == null){
			pt1 = p;
			return;
		}

        //r_t è la distanza di pt1 dal nuovo punto
        double r_t = pt1.getDistance(p);
        if (r_t == 0) { //TODO: FIX
            r_t = 1e-9;
        }
        // update lower bound
        // Q contiene le gammas
        ArrayList<Integer> gams = new ArrayList<>(q.keySet());

        //Rimuovo le gammas minori di log(r_t) da tutti gli insiemi
        double minDist = Math.floor(Math.log(r_t) / Math.log(1 + eps));
        for(Integer gam : gams){
            if(gam < minDist){
                cold.remove(gam);
                cnew.remove(gam);
                q.remove(gam);
                r.remove(gam);
            } else {
                break;
            }
        }

        //Se a questo punto q non è vuoto
        if(!q.isEmpty()){
            //Metto nelle gammas >= di log(r_t) pt2 per cold, r e q, mentre in cnew metto pt1
            int low = q.firstKey() -1;
            while(low >= (int) minDist){
                cold.put(low, pt2);
                r.put(low, pt2);
                q.put(low, pt2);
                cnew.put(low, pt1);
                low--;
            }
        }
        // ended updating lower bound

        //ORA HO FINITO UPDATE DEL LOWER BOUND

        // add p for each gamma

        //Per ogni gamma
        int i = (int) minDist;

        //Questo e' M_t, se ci vado sopra tolgo le altre. Equivale a 6*(1+beta)*rt/2. E' un valore iniziale, che viene poi updated nell'algoritmo
        double M_t = 3*(1+eps)*r_t;
        while(true){
            double gamma = Math.pow(1+eps, i);
            // reached upper bound, cleanup and exit
            if(gamma > M_t){
                for(int j = i; j <= q.lastKey(); j++){
                    cold.remove(j);
                    cnew.remove(j);
                    r.remove(j);
                    q.remove(j);
                }
                break;
            }

            // need to rebuild if implicitly stored. Metto in quelli vecchi pt2, in quelli nuovi null
            if(q.isEmpty() || i > q.lastKey()){
                cold.put(i, pt2);
                r.put(i, pt2);
                q.put(i, pt2);
                cnew.put(i, null);
            }

            // delete old points. Se e' espirato il cold corrispondente
            if(cold.get(i).hasExpired(time)){

                //Se non ci sono punti in cnew
                if(cnew.get(i) != null){
                    if(cold.get(i) == q.get(i)){
                        cold.put(i, r.get(i));
                        cnew.put(i, null);
                    }
                    else{
                        cold.put(i, q.get(i));
                        cnew.put(i, null);
                    }
                }
                else{
                    cold.put(i, r.get(i));
                }
            }
            // insert p
            if(cnew.get(i) == null){
                if(p.getDistance(r.get(i)) > gamma){
                    cold.put(i, r.get(i));
                    q.put(i, r.get(i));
                    cnew.put(i, p);
                }
                else if(p.getDistance(cold.get(i)) > gamma){
                    q.put(i, r.get(i));
                    cnew.put(i, p);
                }
            }
            else{
                if(p.getDistance(r.get(i)) > gamma){
                    cold.put(i, r.get(i));
                    q.put(i, r.get(i));
                    cnew.put(i, p);
                }
                else if(p.getDistance(cnew.get(i)) > gamma){
                    cold.put(i, cnew.get(i));
                    q.put(i, r.get(i));
                    cnew.put(i, p);
                }
                else if(p.getDistance(q.get(i)) > gamma){
                    if(cold.get(i) == q.get(i)){
                        cold.put(i, q.get(i));
                        q.put(i, r.get(i));
                        cnew.put(i, p);
                    }
                }
            }

            // update r
            r.put(i, p);

            if(cnew.get(i) != null){ // diameter <= 3*gamma
                M_t = 3*gamma*(1+eps);
            }

            i++;
        }

        pt2 = pt1;
		pt1 = p;
    }

    public int getSize() {
        int size = cold.size()+cnew.size()+q.size()+r.size();
        size += 2;
        return size;
    }


    private final TreeMap<Integer, Point> cold, cnew, q, r;
    private Point pt2, pt1;
    private final double eps;
}
