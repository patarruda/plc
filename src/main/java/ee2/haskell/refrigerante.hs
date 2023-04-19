import Control.Concurrent
import Text.Printf

main :: IO ()
main = do
    
    contador <- newMVar 1
    pepiseCola <- newMVar 2000
    guaranaPoloNorte <- newMVar 2000
    guaranaQuate <- newMVar 2000
    controle <- newMVar True


    forkIO (consumidor controle pepiseCola contador "Pepise-Cola")
    forkIO  (repositor pepiseCola controle "Pepise-Cola")

    forkIO (consumidor controle guaranaPoloNorte contador "Guarana Polo Norte")
    forkIO (repositor guaranaPoloNorte controle "Guarana Polo Norte")

    forkIO (consumidor controle guaranaQuate contador "Guarana Quate")
    forkIO (repositor guaranaQuate controle "Guarana Quate")

    return ()

-- Talvez de pra juntar os 3 em uma só (tentar dps)
consumidor :: MVar Bool -> MVar Int -> MVar Int -> String -> IO ()
consumidor controle quantidade contador marcaRefri 
        = do
            -- Setando a variável de controle, passando o contador para a variável "clienteAtual" e pegando a quantidade de refrigerante atual.
            usando <- takeMVar controle  -- Caso "controle" ainda não tenha recebido o seu valor novamente, a thread ficará parada esperando, significando que ele precisa ser reabastecido.
            clienteAtual <- takeMVar contador
            refrigerante <- takeMVar quantidade
            if (refrigerante >= 1000) && (clienteAtual <= 22) then -- Rever essa condição pois não esta passando de 30
                do
                    threadDelay 1000000 
                    printf "O cliente %d do refrigerante %s está reabastecendo seu copo\n" clienteAtual marcaRefri
                    putMVar quantidade (refrigerante - 300)
                    putMVar contador (clienteAtual + 1)
                    putMVar controle usando
                    consumidor controle quantidade contador marcaRefri
            else do putMVar controle usando
                    putMVar contador clienteAtual
                    
repositor :: MVar Int -> MVar Bool -> String -> IO ()
repositor quantidade controle marcaRefri
         = do
            usando <- takeMVar controle
            refrigerante <- takeMVar quantidade
            if (refrigerante < 1000) then
                do
                    threadDelay 1500000
                    printf "O refrigerante %s foi reabastecido com 1000 ml, e agora possui %d ml\n" marcaRefri (refrigerante + 1000)
                    putMVar quantidade (refrigerante + 1000)
            else do putMVar quantidade refrigerante
            putMVar controle usando
            repositor quantidade controle marcaRefri
                