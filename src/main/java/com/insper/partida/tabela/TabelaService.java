package com.insper.partida.tabela;

import com.insper.partida.equipe.TeamService;
import com.insper.partida.equipe.dto.TeamReturnDTO;
import com.insper.partida.game.Game;
import com.insper.partida.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TabelaService {

    @Autowired
    private TeamService teamService;

    @Autowired
    private GameService gameService;

    @Autowired
    private TabelaRepository tabelaRepository;
    public List<TimeDTO> getTabela() {

        List<TeamReturnDTO> times = teamService.listTeams();
        List<TimeDTO> response = new ArrayList<>();

        for (TeamReturnDTO time : times) {

            List<Game> games = gameService.getGameByTeam(time.getIdentifier());

            TimeDTO timeDTO = new TimeDTO();
            timeDTO.setNome(time.getName());

            for (Game game : games) {
                timeDTO.setPontos(timeDTO.getPontos() + verificaResultado(time, game));
                timeDTO.setVitorias(timeDTO.getVitorias() + (verificaVitorias(time, game) ? 1 : 0));
                timeDTO.setDerrotas(timeDTO.getDerrotas() + (verificaDerrotas(time, game) ? 1 : 0));
                timeDTO.setEmpates(timeDTO.getEmpates() + (verificaEmpates(time, game) ? 1 : 0));
                timeDTO.setGolsPro(timeDTO.getGolsPro() + verificaGolsPro(time, game));
                timeDTO.setGolsContra(timeDTO.getGolsContra()  + verificaGolsContra(time, game));
                timeDTO.setJogos(timeDTO.getJogos() + 1);
            }
            response.add(timeDTO);

        }
        response.sort(Comparator.comparingInt(TimeDTO::getPontos).reversed());
        return response;

    }

    private Integer verificaResultado(TeamReturnDTO time, Game game) {
        if (game.getScoreHome() == game.getScoreAway()) {
            return 1;
        }
        if (game.getHome().equals(time.getIdentifier()) && game.getScoreHome() > game.getScoreAway()) {
            return 3;
        }
        if (game.getAway().equals(time.getIdentifier()) && game.getScoreAway() > game.getScoreHome()) {
            return 3;
        }
        return 0;
    }

    private Integer verificaGolsPro(TeamReturnDTO time, Game game) {
        if (game.getHome().equals(time.getIdentifier())) {
            return game.getScoreHome();
        }
        return game.getScoreAway();
    }

    private Integer verificaGolsContra(TeamReturnDTO time, Game game) {
        if (game.getHome().equals(time.getIdentifier())) {
            return game.getScoreAway();
        }
        return game.getScoreHome();
    }

    private boolean verificaVitorias(TeamReturnDTO time, Game game) {
        if (game.getHome().equals(time.getIdentifier()) && game.getScoreHome() > game.getScoreAway()) {
            return true;
        }
        if (game.getAway().equals(time.getIdentifier()) && game.getScoreAway() > game.getScoreHome()) {
            return true;
        }
        return false;
    }

    private boolean verificaDerrotas(TeamReturnDTO time, Game game) {
        if (game.getHome().equals(time.getIdentifier()) && game.getScoreHome() < game.getScoreAway()) {
            return true;
        }
        if (game.getAway().equals(time.getIdentifier()) && game.getScoreAway() < game.getScoreHome()) {
            return true;
        }
        return false;
    }

    private boolean verificaEmpates(TeamReturnDTO time, Game game) {
        return Objects.equals(game.getScoreHome(), game.getScoreAway());
    }

    public void updateTabelaForGame(Game game) {
        // Atualizando para o time da casa
        Tabela timeCasa = tabelaRepository.findByNome(game.getHome());
        if (timeCasa == null) {
            timeCasa = new Tabela();
            timeCasa.setNome(game.getHome());
        }
        atualizaDadosTabelaComBaseNoJogo(timeCasa, game.getScoreHome(), game.getScoreAway());

        // Atualizando para o time visitante
        Tabela timeVisitante = tabelaRepository.findByNome(game.getAway());
        if (timeVisitante == null) {
            timeVisitante = new Tabela();
            timeVisitante.setNome(game.getAway());
        }
        atualizaDadosTabelaComBaseNoJogo(timeVisitante, game.getScoreAway(), game.getScoreHome());

        // Salve as atualizações no banco de dados
        tabelaRepository.save(timeCasa);
        tabelaRepository.save(timeVisitante);
    }

    private void atualizaDadosTabelaComBaseNoJogo(Tabela time, int golsFeitos, int golsSofridos) {
        time.setJogos(time.getJogos() + 1);
        time.setGolsPro(time.getGolsPro() + golsFeitos);
        time.setGolsContra(time.getGolsContra() + golsSofridos);

        if (golsFeitos > golsSofridos) {
            time.setVitorias(time.getVitorias() + 1);
            time.setPontos(time.getPontos() + 3);
        } else if (golsFeitos < golsSofridos) {
            time.setDerrotas(time.getDerrotas() + 1);
        } else {
            time.setEmpates(time.getEmpates() + 1);
            time.setPontos(time.getPontos() + 1);
        }
    }

}
