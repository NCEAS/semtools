package org.ecoinformatics.sms;

import org.ecoinformatics.sms.*;
import org.ecoinformatics.sms.ontology.*;

public class Test
{
  public static void main(String[] args)
  {
    try
    {
      String uri = args[0];
      SMS sms = new SMS();
      OntologyManager ontologyManager = sms.getOntologyManager();
      ontologyManager.importOntology(uri);
      ontologyManager.classify();
      System.out.println("classes: " + ontologyManager.getNamedClasses());
    }
    catch(Exception e)
    {
      System.out.println("Error: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
