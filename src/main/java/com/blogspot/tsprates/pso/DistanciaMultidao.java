package com.blogspot.tsprates.pso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanciaMultidao implements Comparator<Particula>
{

    public Map<Particula, Double> ranking = new HashMap<>();

    public void atualiza(List<Particula> parts)
    {
        List<Particula> tempPart = new ArrayList<>(parts);
        int numParts = tempPart.size();

        for (int i = 0; i < numParts; i++)
        {
            ranking.put(tempPart.get(i), 0.0);
        }

        if (numParts == 1)
        {
            ranking.put(tempPart.get(0), Double.MAX_VALUE);
        }

        if (numParts == 2)
        {
            ranking.put(tempPart.get(numParts - 1), Double.MAX_VALUE);
        }

        if (numParts > 2)
        {

            double[] objMax = tempPart.get(0).fitness();
            double[] objMin = tempPart.get(numParts - 1).fitness();

            for (int j = 1, len = numParts - 1; j < len; j++)
            {
                double[] a = tempPart.get(j + 1).fitness();
                double[] b = tempPart.get(j - 1).fitness();

                Particula particula = tempPart.get(j);

                double d = 0.0;
                for (int i = 0; i < 2; i++)
                {
                    d += (a[i] - b[i])
                            / (objMax[i] - objMin[i] + Double.MIN_VALUE);
                }

                ranking.put(particula, d);
            }
        }

    }

    @Override
    public int compare(Particula p1, Particula p2)
    {
        Double temp1 = ranking.get(p1);
        Double temp2 = ranking.get(p2);

        double soma = temp1 - temp2;

        if (soma == 0)
        {
            return 0;
        }
        else if (soma > 0)
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }
}
