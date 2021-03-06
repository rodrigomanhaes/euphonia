Euphonia: multiplatform and multidatabase data migration
========================================================

Euphonia is an API for data migration in relational databases. Euphonia is written in Java/JDBC, multiplatform and multidatabase.

You must specify: origin and target database connection string, origin and target DBMS, table and field names. The meaning of methods is straightforward::

    private static final String
  	    DATABASE_SOURCE = "jdbc:derby:temp/source;create=true",
	      DATABASE_TARGET = "jdbc:derby:temp/target;create=true";

    new Migration()
        .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
        .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
        .table("tb_person").to("person")
            .field("field_id").to("id")
            .field("field_name").to("name")
            .field("field_cpf").to("cpf")
            .field("field_document").to("document")
        .run();


If all fields in the source and target are equal and all fields must be migrated, a shortcut can be used::

  new Migration()
      .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
      .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
      .table("tb_person").to("person")
          .allFields()
      .run();


Arbitrary conversions can be applied to field copies through implementing the TransferStrategy interface. transfer method receives and returns arrays for supporting multiple field conversion. Uppercase, lowercase and capitalize transformations are included in the CaseTransformation class::

    TransferStrategy reverse = new TransferStrategy()
    {
	    @Override
	    public Object[] transfer(Object... values)
	    {
		    return array(reverseString((String) values[0]));
	    }

    };

    new Migration()
	    .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
	    .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
	    .table("tb_person").to("person")
		    .field("field_id").to("id")
		    .field("field_name").to("name").withTransformation(reverse)
		    .field("field_cpf").to("cpf")
		    .field("field_document").to("document")
	    .run();


The default behavior of Migration.run() method is to delete all records before migrating. But if migration must be incremental, it is simple::

    new Migration()
	    .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
	    .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
	    .table("tb_person").to("person")
		    .allFields()
		    .incremental()
	    .run();


A selection constraint can be added. A native SQL, complete where clause must be specified (but not the 'where' keyword itself)::

    new Migration()
	    .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
	    .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
	    .table("tb_person").to("person")
		    .allFields()
		    .where("field_name like '%Beck'")
	    .run();


Many fields can be migrated into one, by applying a transformation strategy. The concat strategy used in the example is provided by TransferFactory class::

    new Migration()
	    .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
	    .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
	    .table("tb_person").to("person")
		    .field("field_id").to("id")
		    .fields("field_name", "field_cpf", "field_document").to("data")
			    .withTransformation(concat(","))
	    .run();


One field can be migrated to many, by applying a transformation strategy. The split strategy used in the example is provided by TransferFactory class::

    new Migration()
	    .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
	    .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
	    .table("tb_person").to("person")
		    .field("id").to("id")
		    .fields("data").to("name", "cpf", "document")
			    .withTransformation(split(","))
	    .run();


Many fields can be migrated to many fields (the input and output number don't need to be equal). The "threeToTwo" strategy is only illustrative::

    new Migration()
	    .from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
	    .to(DATABASE_TARGET).in(DERBY_EMBEDDED)
	    .table("tb_person").to("person")
		    .field("field_id").to("id")
		    .fields("field_name", "field_cpf", "field_document").to("name_cpf", "cpf_document")
			    .withTransformation(threeToTwo)
	    .run();


Current dependencies are JUnit 4, Apache Commons Logging, Apache Log4J and Apache Derby (only for testing). The project can be built with Maven.

