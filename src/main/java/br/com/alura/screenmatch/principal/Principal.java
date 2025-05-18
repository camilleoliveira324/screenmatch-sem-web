package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverterDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverterDados conversor = new ConverterDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_Key = "&apikey=5bc273ce";

    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository serieRepository;

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        int opcao = -1;
        while(opcao != 0) {
            var menu = """
            1 - Buscar séries
            2 - Buscar episódios
            3 - Listar séries buscadas

            0 - Sair                                 
            """;

            System.out.println(menu);
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }
    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        serieRepository.save(serie);
        System.out.println(serie);
    }
    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para buscar:");
        String nomeSerie = sc.nextLine();
        String json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_Key);
        DadosSerie dados = conversor.converterDados(json, DadosSerie.class);
        return dados;
    }

    private void listarSeriesBuscadas() {
        List<Serie> series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarEpisodioPorSerie() {
        DadosSerie dadosSerie = getDadosSerie();
        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i < dadosSerie.totalTemporadas(); i++) {
            String json = consumoAPI.obterDados(ENDERECO + dadosSerie.titulo().replace(" ", "+" + "&season" + i + API_Key));
            DadosTemporada dadosTemporada = conversor.converterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);
    }

}
