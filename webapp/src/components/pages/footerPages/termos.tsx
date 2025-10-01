import "./pagesfooter.css";

const Termos: React.FC = () => {
  return (
    <div className="dados-conteiner-footer-pages">
      <h2 className="pages-h2">Termos de Privacidade</h2>

      <p className="pages-p">
        A privacidade dos nossos usuários é prioridade. Todos os dados coletados
        durante o uso da plataforma são tratados conforme a legislação vigente
        <span className="p-destaque">
          {" "}
          (LGPD – Lei Geral de Proteção de Dados).{" "}
        </span>{" "}
        As informações pessoais são utilizadas apenas para fins de cadastro,
        segurança e aprimoramento dos serviços, nunca sendo compartilhadas com
        terceiros sem autorização do usuário.
      </p>
    </div>
  );
};

export default Termos;
