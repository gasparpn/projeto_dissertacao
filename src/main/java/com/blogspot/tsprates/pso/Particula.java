package com.blogspot.tsprates.pso;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Classe Partícula.
 *
 * @author thiago
 */
public class Particula implements Comparable<Particula>
{

    private Set<String> posicao = new HashSet<>();

    private String classe;

    private final FronteiraPareto fp;

    private double[] fitness;

    private final InterfaceFitness calculadorFitness;

    private Set<Particula> pbest = new LinkedHashSet<>();

    /**
     * Construtor.
     *
     * @param posicao
     * @param classe
     * @param fit
     * @param fp
     */
    public Particula(Set<String> posicao, String classe, InterfaceFitness fit,
            FronteiraPareto fp)
    {
        this.posicao = new HashSet<>(posicao);
        this.fp = fp;
        this.classe = classe;
        this.calculadorFitness = fit;
        final Particula that = this;
        this.fitness = this.calculadorFitness.calcular(that);
    }

    /**
     *
     * @param p
     */
    public Particula(Particula p)
    {
        this(p.posicao, p.classe, p.calculadorFitness, p.fp);
    }

    /**
     * Get posição da partícula.
     *
     * @return Lista de String WHERE de nova posição.
     */
    public Set<String> posicao()
    {
        return posicao;
    }

    /**
     * Seta nova posição da partícula.
     *
     * @param posicao Lista de String WHERE de nova posição.
     */
    public void setPosicao(Collection<String> posicao)
    {
        this.posicao = new HashSet<>(posicao);
        this.fitness = this.calculadorFitness.calcular(this);
    }

    /**
     * Retorna uma cláusula WHERE SQL correspondente a posição da partícula.
     *
     * @return String WHERE SQL.
     */
    public String whereSql()
    {
        return join(posicao);
    }

    /**
     * Retorna a classe da partícula.
     *
     * @return String representando a partícula.
     */
    public String classe()
    {
        return classe;
    }

    /**
     * Seta a classe da partícula.
     *
     * @param classe Classe da partícula.
     */
    public void setClasse(String classe)
    {
        this.classe = classe;
    }

    /**
     * Retorna a dimensão (tamanho) da cláusula WHERE.
     *
     * @return Tamanho da cláusula WHERE.
     */
    public int numWhere()
    {
        return posicao().size();
    }

    /**
     * Retorna fitness da partícula.
     *
     * @return Array de doubles.
     */
    public double[] fitness()
    {
        return fitness;
    }

    /**
     * Retorna string de cláusulas WHERE.
     *
     * @param l
     * @return String de cláusulas WHERE.
     */
    private String join(Set<String> l)
    {
        return "(" + StringUtils.join(l, ") AND (") + ")";
    }

    @Override
    public String toString()
    {
        return join(this.posicao);
    }

    /**
     *
     * @return
     */
    public Set<Particula> getPbest()
    {
        return pbest;
    }

    /**
     *
     * @param pbest
     */
    public void setPbest(List<Particula> pbest)
    {
        this.pbest = new HashSet<>(pbest);
    }

    /**
     *
     */
    public void atualizaPbest()
    {
        fp.atualizarParticulasNaoDominadas(pbest, this);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.posicao);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        final Particula other = (Particula) obj;

        return Objects.equals(this.posicao, other.posicao);
    }

    @Override
    public int compareTo(Particula part)
    {
        double[] pfit = part.fitness();
        if (fitness[0] == pfit[0])
        {
            if (fitness[1] < pfit[1])
            {
                return 1;
            }
            else if (fitness[1] < pfit[1])
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else if (fitness[0] < pfit[0])
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }

}
