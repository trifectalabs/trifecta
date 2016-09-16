package modules

import kiambogo.scrava.{ClientFactory, ClientFactoryImpl}

trait ScravaModule {
  def scravaClientFactory: ClientFactory = new ClientFactoryImpl 
}
