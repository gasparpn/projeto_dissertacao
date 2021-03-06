package com.github.tsprates.pso;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.github.tsprates.pso.FronteiraPareto.atualizarParticulasNaoDominadas;
import static com.github.tsprates.pso.FronteiraPareto.verificarNumParticulas;

/**
 * Classe Partícula.
 *
 * @author thiago
 */
public class Particula
                implements Comparable<Particula>
{

    private final Fitness calculadoraFitness;

    private final Random random;

    private Set<String> posicao;

    private String strPos;

    private String classe;

    private double[] fitness;

    private Set<Particula> pbest;

    /**
     * Construtor.
     *
     * @param posicao Conjunto de cláusulas WHERE que representa a posição da partícula.
     * @param classe  Rótulo (nicho) da partícula.
     * @param fitness Calculadora de fitness.
     * @param random  Gerador de números aleatórios.
     */
    public Particula( Set<String> posicao, String classe, Fitness fitness, Random random )
    {
        this.posicao = new TreeSet<>( posicao );
        this.strPos = join( posicao );
        this.classe = classe;
        this.pbest = new TreeSet<>();
        this.random = random;

        this.calculadoraFitness = fitness;

        final Particula that = this;
        this.fitness = calculadoraFitness.calcular( that );
    }

    /**
     * Construtor.
     *
     * @param p Partícula.
     */
    public Particula( Particula p )
    {
        this( p.posicao, p.classe, p.calculadoraFitness, p.random );
    }

    /**
     * Posição da partícula.
     *
     * @return Lista de String WHERE de nova posição.
     */
    public Set<String> posicao()
    {
        return posicao;
    }

    /**
     * Seta uma nova posição da partícula.
     *
     * @param posicao Coleção de Strings da nova posição.
     */
    public void setPosicao( Collection<String> posicao )
    {
        this.posicao = new TreeSet<>( posicao );
        this.strPos = join( this.posicao );
    }

    /**
     * Avaliar fitness da partícula.
     */
    public void avaliar()
    {
        this.fitness = calculadoraFitness.calcular( this );
    }

    /**
     * Retorna uma cláusula WHERE SQL correspondente a posição da partícula.
     *
     * @return String WHERE SQL.
     */
    public String whereSql()
    {
        return strPos;
    }

    @Override
    public String toString()
    {
        return strPos;
    }

    /**
     * Retorna a classe (nicho) da partícula.
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
    public void setClasse( String classe )
    {
        this.classe = classe;
    }

    /**
     * Retorna o tamanho da cláusula WHERE.
     *
     * @return Tamanho da cláusula WHERE (número de condições).
     */
    public int numWhere()
    {
        return posicao().size();
    }

    /**
     * Retorna fitness da partícula. É necessária a avaliação {@link #avaliar}.
     *
     * @return Array de doubles.
     */
    public double[] fitness()
    {
        return fitness;
    }

    /**
     * Retorna cláusula WHERE.
     *
     * @param where Cláusula WHERE.
     * @return String de cláusulas WHERE.
     */
    private String join( Set<String> where )
    {
        return "(" + StringUtils.join( where, ") AND (" ) + ")";
    }

    /**
     * Retorna pbest.
     *
     * @return Partícula pbest.
     */
    public Collection<Particula> getPbest()
    {
        return pbest;
    }

    /**
     * Seta pbest.
     *
     * @param pbest Partícula pbest.
     */
    public void setPbest( List<Particula> pbest )
    {
        this.pbest = new TreeSet<>( pbest );
    }

    /**
     * Atualiza pbest (memória ou história da partícula).
     */
    public void atualizarPbest()
    {
        atualizarParticulasNaoDominadas( pbest, this );
        this.pbest = new TreeSet<>( pbest );

        verificarNumParticulas( random, this.pbest );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( fitness );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null )
        {
            return false;
        }

        if ( getClass() != obj.getClass() )
        {
            return false;
        }

        final Particula outra = (Particula) obj;
        return fitness[0] == outra.fitness[0] && fitness[1] == outra.fitness[1];
    }

    @Override
    public int compareTo( Particula part )
    {
        double[] pfit = part.fitness();

        if ( fitness[1] == pfit[1] )
        {
            if ( fitness[0] == pfit[0] )
            {
                return 0;
            }
            else if ( fitness[0] < pfit[0] )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
        else if ( fitness[1] < pfit[1] )
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }

    /**
     * Gera uma cópia da partícula.
     *
     * @return Partícula clonada.
     */
    public Particula clonar()
    {
        return new Particula( posicao, classe, calculadoraFitness, random );
    }
}
