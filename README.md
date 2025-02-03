# Dokumentation för refaktorisering av PaymentProcessor

I klassen finns det tre beroenden:
- PaymentApi används för att hantera betalningar 
- DatabaseConnection används för kontakt med databasen
- EmailService används för att skicka meddelanden till epostadressen

Jag skapade interfaces för dessa för att kunna mocka dem i testerna. Anledningen till att det är enklare med interfaces för mockning är att de beroenden som klasser skulle ha, såsom korrekta inparametrar inte behövs. Man behöver inte bry sig om externa beroenden i klassen.

Jag skapade även en klass för PaymentApiResponse för att kunna hantera resultatet/responsen - om betalningen lyckades eller ej. 

Jag la sedan till de relevata metoderna i interfacen som finns i PaymentProcessor:

I EmailService skapade jag metoden sendPaymentConfirmation där tanken är att metoden i en passande klass ska skicka en betalningsbekräftelse till betalaren. Inparametrar är mejladressen samt belopp(amount). 

I DatabaseConnection skapade jag metoden getInstanse skickar en SQL-uppdatering och en executeUpdate som avslutar uppkopplingen med databasen.

I PaymentApi finns PaymentApi finns en metod för charge som är tänkt att användas för betalning. Metoden returnerar ett object av PaymentApiResponse som i sin tur meddelar om betalningen lyckats eller inte. 
