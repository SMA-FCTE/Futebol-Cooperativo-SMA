import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;

public class ZagueiroAgent extends Agent {

    private String time;

    protected void setup() {
        // identifica o time
        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            System.out.println(getLocalName() + ": erro - é necessário informar o argumento 'A' ou 'B' para indicar o time.");
            doDelete();
            return;
        }

        time = (String) args[0];
        System.out.println(getLocalName() + " criado para o time " + time + ".");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                receberMensagens(this);
            }
        });
    }

    // FUNÇÕES ---------------------------------------------------------------------

    private void receberMensagens(CyclicBehaviour behaviour) {
        ACLMessage mensagem = receive();

        if (mensagem != null) {
            String conteudo = mensagem.getContent();

            if (conteudo.startsWith("passe")) {
                String timeDoPasse = conteudo.split("=")[1];
                if (timeDoPasse.equalsIgnoreCase(time)) {
                    processarPasse(false);
                }
            } else if (conteudo.startsWith("roubo")) {
                String timeDoRoubo = conteudo.split("=")[1];
                if (timeDoRoubo.equalsIgnoreCase(time)) {
                    System.out.println("Zagueiro " + time + ": recuperou a posse após roubo!");
                    processarPasse(true);
                }
            }
        } else {
            behaviour.block();
        }
    }

    private void processarPasse(boolean aposRoubo) {
        if (!aposRoubo)
            System.out.println("Zagueiro " + time + ": recebeu passe do goleiro.");

        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }

        int chance = (int) (Math.random() * 101);
        if (chance <= 20) {
            System.out.println("Zagueiro " + time + ": perdeu a bola! Atacante adversário roubou (20% chance).");
            passarPosseAoAdversario();
        } else {
            System.out.println("Zagueiro " + time + ": passe bem-sucedido! Enviando bola para o meio-campo...");
            enviarPasse();
        }
    }

    private void enviarPasse() {
        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }

        ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
        mensagem.addReceiver(new AID("meia" + time, AID.ISLOCALNAME));
        mensagem.setContent("passe;time=" + time);
        send(mensagem);

        System.out.println("Zagueiro " + time + ": passe enviado para o meia" + time + "!");
    }

    private void passarPosseAoAdversario() {
        try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }

        String timeAdversario = time.equalsIgnoreCase("A") ? "B" : "A";
        ACLMessage mensagem = new ACLMessage(ACLMessage.INFORM);
        mensagem.addReceiver(new AID("atacante" + timeAdversario, AID.ISLOCALNAME));
        mensagem.setContent("roubo;time=" + timeAdversario);
        send(mensagem);

        System.out.println("Zagueiro " + time + ": perdeu a posse para atacante" + timeAdversario + ".");
    }
}
