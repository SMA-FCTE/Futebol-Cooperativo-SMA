const PASS_REFUSAL_REASONS: Record<string, string> = {
  PASSADOR_SEM_BOLA: 'Passador nao esta mais com a bola',
  POSSE_ALTERADA_ANTES_DA_RESPOSTA: 'Posse mudou antes da resposta ao pedido',
  SISTEMA_INDISPONIVEL: 'Estado da partida indisponivel',
  RECEPTOR_NAO_ENCONTRADO: 'Receptor nao encontrado',
  MESMO_JOGADOR: 'Passador e receptor sao o mesmo jogador',
  RECEPTOR_OUTRO_TIME: 'Receptor pertence ao outro time',
  RECEPTOR_EM_PENALIDADE: 'Receptor esta em penalidade',
  RECEPTOR_EM_COOLDOWN: 'Receptor esta em cooldown',
  RECEPTOR_FORA_DO_RAIO: 'Receptor esta fora do raio de passe',
  ATACANTE_NAO_PASSA_BOLA: 'Atacante nao realiza passes',
  PASSADOR_NAO_E_ALIADO: 'Passador nao pertence ao mesmo time',
  MOTIVO_NAO_INFORMADO: 'Motivo nao informado',
}

export function formatPassRefusalReason(reason: string | null): string {
  if (!reason) {
    return '-'
  }

  return PASS_REFUSAL_REASONS[reason] ?? reason
}

export function formatPassStatus(status: string | null): string {
  if (!status) {
    return 'Aguardando passe'
  }

  return status === 'CANCELADO' ? 'Posse mudou antes da resposta' : status
}
