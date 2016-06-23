package com.blogspot.tsprates.pso;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

/**
 * Particles Swarm Optimization (PSO).
 *
 * @author thiago
 */
public class App
{

    /**
     * Main.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("--------------------------------------------");
        System.out.println(" Projeto de Dissertação Mestrado            ");
        System.out.println(" Implementação PSO                          ");
        System.out.println("--------------------------------------------");

        if (args.length > 0 && Files.exists(Paths.get(args[0])))
        {
            Connection db = new DbFactory().conectar();
            Properties config = carregarArquivoConfig(args[0]);

            final Formatador formatador = new Formatador();

            final int K = 10;
            Pso pso = new Pso(db, config, formatador, K);

            List<Double> efetPSO = new ArrayList<>();
            List<Double> efetJ48 = new ArrayList<>();
            List<Double> efetSMO = new ArrayList<>();
            List<Double> efetRBF = new ArrayList<>();

            final int EXEC = 30;
            for (int iter = 0; iter < EXEC; iter++)
            {
                System.out.println();
                System.out.println("Execução: " + (iter + 1));
                System.out.println();

                pso.carregar();
//                pso.mostrarResultados();

                efetPSO.add(pso.getResultado());

                Weka weka = new Weka(pso.getKPasta(), K, config);
                double[] efetArray = weka.getEfetividadeArray();
                efetJ48.add(efetArray[0]);
                efetSMO.add(efetArray[1]);
                efetRBF.add(efetArray[2]);
            }

            mostrarValorMedioExec(formatador, efetPSO, efetJ48, efetSMO, efetRBF);

            wilcoxonTeste(formatador, efetPSO, efetJ48, efetSMO, efetRBF);

            mostrarGrafico(config, efetPSO, efetJ48, efetSMO, efetRBF);
        }
        else
        {
            System.err.println(
                    "É necessário definir um arquivo de configuração.");
        }
    }

    /**
     * Carrega arquivo de configurações.
     *
     * @param configFile Configurations
     * @return Properties
     */
    private static Properties carregarArquivoConfig(String configFile)
    {
        try (FileInputStream fis = new FileInputStream(configFile))
        {
            Properties prop = new Properties();
            prop.load(fis);
            return prop;
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Arquivo de configurações não encontrado.", e);
        }
    }

    /**
     * Gráfico valor médio execuções.
     *
     * @param config
     * @param efetPSO
     * @param efetJ48
     * @param efetSMO
     * @param efetRBF
     */
    private static void mostrarGrafico(Properties config, List<Double> efetPSO,
            List<Double> efetJ48, List<Double> efetSMO, List<Double> efetRBF)
    {
        List<Double> tempEfetPSO = new ArrayList<>(efetPSO);
        List<Double> tempEfetJ48 = new ArrayList<>(efetJ48);
        List<Double> tempEfetSMO = new ArrayList<>(efetSMO);
        List<Double> tempEfetRBF = new ArrayList<>(efetRBF);

        // Ordena efetividade
        Collections.sort(tempEfetPSO);
        Collections.sort(tempEfetJ48);
        Collections.sort(tempEfetSMO);
        Collections.sort(tempEfetRBF);

        // Gráfico Efetividade Média
        final String tituloGrafico = StringUtils.capitalize(config.getProperty("tabela"));
        final String eixoX = "Execução";
        final String eixoY = "Sensibilidade x Especificidade";

        Grafico g = new Grafico(tituloGrafico, eixoX, eixoY);
        g.adicionarSerie("MOPSO", tempEfetPSO);
        g.adicionarSerie("J48", tempEfetJ48);
        g.adicionarSerie("SMO", tempEfetSMO);
        g.adicionarSerie("RBF", tempEfetRBF);
        g.mostra();
    }

    /**
     * Teste não-paramétrico para verificar se os resultados médios pertencem a
     * mesma distribuição estatística.
     *
     * @param fmt
     * @param efetPSO
     * @param efetJ48
     * @param efetSMO
     * @param efetRBF
     */
    private static void wilcoxonTeste(final Formatador fmt,
            List<Double> efetPSO, List<Double> efetJ48, List<Double> efetSMO,
            List<Double> efetRBF)
    {
        // Wilcoxon Test
        WilcoxonSignedRankTest w = new WilcoxonSignedRankTest();

        Double[] tempArrPSO = efetPSO.toArray(new Double[0]);
        Double[] tempArrJ48 = efetJ48.toArray(new Double[0]);
        Double[] tempArrSMO = efetSMO.toArray(new Double[0]);
        Double[] tempArrRBF = efetRBF.toArray(new Double[0]);

        double[] arrPSO = ArrayUtils.toPrimitive(tempArrPSO);
        double[] arrJ48 = ArrayUtils.toPrimitive(tempArrJ48);
        double[] arrSMO = ArrayUtils.toPrimitive(tempArrSMO);
        double[] arrRBF = ArrayUtils.toPrimitive(tempArrRBF);

        String pvaluePSO_J48 = fmt.formatar(w.wilcoxonSignedRankTest(arrPSO, arrJ48, false));
        String pvaluePSO_SMO = fmt.formatar(w.wilcoxonSignedRankTest(arrPSO, arrSMO, false));
        String pvaluePSO_RBF = fmt.formatar(w.wilcoxonSignedRankTest(arrPSO, arrRBF, false));
        String pvalueJ48_SMO = fmt.formatar(w.wilcoxonSignedRankTest(arrJ48, arrSMO, false));
        String pvalueJ48_RBF = fmt.formatar(w.wilcoxonSignedRankTest(arrJ48, arrRBF, false));
        String pvalueSMO_RBF = fmt.formatar(w.wilcoxonSignedRankTest(arrSMO, arrRBF, false));

        System.out.println("\n\nTeste Wilcoxon MOPSO:\n");

        String strFormat = "%-10s %-10s %-10s %-10s %-10s\n";
        
        System.out.printf(strFormat, "", "MOPSO", "J48", "SMO", "RBF");
        System.out.printf(strFormat, "MOPSO", "-", pvaluePSO_J48, pvaluePSO_SMO, pvaluePSO_RBF);
        System.out.printf(strFormat, "J48", pvaluePSO_J48, "-", pvalueJ48_SMO, pvalueJ48_RBF);
        System.out.printf(strFormat, "SMO", pvaluePSO_SMO, pvalueJ48_SMO, "-", pvalueSMO_RBF);
        System.out.printf(strFormat, "RBF", pvaluePSO_RBF, pvalueJ48_RBF, pvalueSMO_RBF, "-");
    }

    /**
     * Imprime a média e desvio padrão das execuções dos algoritmos.
     *
     * @param fmt
     * @param efetPSO
     * @param efetJ48
     * @param efetSMO
     * @param efetRBF
     */
    private static void mostrarValorMedioExec(final Formatador fmt,
            List<Double> efetPSO, List<Double> efetJ48, List<Double> efetSMO,
            List<Double> efetRBF)
    {
        SummaryStatistics statsPSO = new SummaryStatistics();
        for (int i = 0, size = efetPSO.size(); i < size; i++)
        {
            statsPSO.addValue(efetPSO.get(i));
        }

        SummaryStatistics statsJ48 = new SummaryStatistics();
        for (int i = 0, size = efetJ48.size(); i < size; i++)
        {
            statsJ48.addValue(efetJ48.get(i));
        }

        SummaryStatistics statsSMO = new SummaryStatistics();
        for (int i = 0, size = efetSMO.size(); i < size; i++)
        {
            statsSMO.addValue(efetSMO.get(i));
        }

        SummaryStatistics statsRBF = new SummaryStatistics();
        for (int i = 0, size = efetRBF.size(); i < size; i++)
        {
            statsRBF.addValue(efetRBF.get(i));
        }

        System.out.println();
        System.out.printf("%-10s %-10s %-10s\n\n", "Alg.", "Méd.", "Desv.");

        String strFormat = "%-10s %-10s %-10s\n";
        
        System.out.printf(strFormat,
                "MOPSO",
                fmt.formatar(statsPSO.getMean()),
                fmt.formatar(statsPSO.getStandardDeviation()));

        System.out.printf(strFormat,
                "J48",
                fmt.formatar(statsJ48.getMean()),
                fmt.formatar(statsJ48.getStandardDeviation()));

        System.out.printf(strFormat,
                "SMO",
                fmt.formatar(statsSMO.getMean()),
                fmt.formatar(statsSMO.getStandardDeviation()));

        System.out.printf(strFormat,
                "RBF",
                fmt.formatar(statsRBF.getMean()),
                fmt.formatar(statsRBF.getStandardDeviation()));
    }
}
