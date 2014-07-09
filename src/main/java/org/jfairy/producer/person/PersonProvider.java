package org.jfairy.producer.person;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import org.apache.commons.lang3.RandomStringUtils;
import org.jfairy.data.DataMaster;
import org.jfairy.producer.BaseProducer;
import org.jfairy.producer.DateProducer;
import org.jfairy.producer.company.Company;
import org.jfairy.producer.company.CompanyProvider;
import org.joda.time.DateTime;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.stripAccents;
import static org.jfairy.producer.person.Person.Sex.FEMALE;
import static org.jfairy.producer.person.Person.Sex.MALE;

public class PersonProvider implements Provider<Person> {

	public static final int MIN_AGE = 1;
	public static final int MAX_AGE = 100;

	private Person.Sex sex;
	private String telephoneNumberFormat;
	private Integer age;
	private DateTime dateOfBirth;

	private boolean randomBoolean() {
		return baseProducer.trueOrFalse();
	}

	@Override
	public Person get() {

		if (sex == null) {
			sex = randomBoolean() ? MALE : FEMALE;
		}

		String firstName = dataMaster.getValuesOfType(FIRST_NAME, sex.name());
		String middleName = randomBoolean() ? dataMaster.getValuesOfType(FIRST_NAME, sex.name()) : "";
		String lastName = dataMaster.getValuesOfType(LAST_NAME, sex.name());
		String email = generateEmail(firstName, lastName);
		String username = generateUsername(firstName, lastName);
		if (telephoneNumberFormat == null) {
			telephoneNumberFormat = dataMaster.getRandomValue(TELEPHONE_NUMBER_FORMATS);
		}
		String telephoneNumber = baseProducer.numerify(telephoneNumberFormat);
		if (age == null) {
			age = baseProducer.randomBetween(MIN_AGE, MAX_AGE);
		}
		if (dateOfBirth == null) {
			dateOfBirth = dateProducer.randomDateInThePast(age);
		}
		String companyEmail = stripAccents(lowerCase(firstName + '.' + lastName + '@' + company.domain()));
		// FIXME: Replace this with baseProducer
		String password = RandomStringUtils.randomAlphanumeric(8);

		return new Person(firstName, middleName, lastName, addressProvider.get(), email, username
				, password, sex, telephoneNumber, dateOfBirth, age,
				nationalIdentityCardNumber.generate(), nationalIdentificationNumber.generate(), company, companyEmail);
	}

	public String nationalIdentificationNumber() {
		return nationalIdentificationNumber.generate(new DateTime(dateOfBirth), sex);
	}

	private String generateEmail(String firstName, String lastName) {
		String temp = "";
		if (randomBoolean()) {
			temp = firstName;
			if (randomBoolean()) {
				temp += ".";
			}
		}
		return stripAccents(lowerCase(temp + lastName + '@' + dataMaster.getRandomValue(PERSONAL_EMAIL)));
	}

	@VisibleForTesting
	static final String FIRST_NAME = "firstNames";

	@VisibleForTesting
	static final String LAST_NAME = "lastNames";
	@VisibleForTesting
	static final String PERSONAL_EMAIL = "personalEmails";
	@VisibleForTesting
	static final String TELEPHONE_NUMBER_FORMATS = "telephone_number_formats";
	private final DataMaster dataMaster;

	private final DateProducer dateProducer;
	private final BaseProducer baseProducer;
	private final NationalIdentificationNumber nationalIdentificationNumber;
	private final NationalIdentityCardNumber nationalIdentityCardNumber;
	private final AddressProvider addressProvider;
	private Company company;

	@Inject
	public PersonProvider(DataMaster dataMaster,
						  DateProducer dateProducer,
						  BaseProducer baseProducer,
						  NationalIdentificationNumber nationalIdentificationNumber,
						  NationalIdentityCardNumber nationalIdentityCardNumber,
						  AddressProvider addressProvider,
						  CompanyProvider companyProvider,

						  @Assisted PersonProperties.PersonProperty... personProperties) {

		this.dataMaster = dataMaster;
		this.dateProducer = dateProducer;
		this.baseProducer = baseProducer;
		this.nationalIdentificationNumber = nationalIdentificationNumber;
		this.nationalIdentityCardNumber = nationalIdentityCardNumber;
		this.addressProvider = addressProvider;
		//fixme - should be created only if needed
		this.company = companyProvider.get();
		for (PersonProperties.PersonProperty personProperty : personProperties) {
			personProperty.apply(this);
		}
	}

	public void setSex(Person.Sex sex) {
		this.sex = sex;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void telephoneNumberFormat(String telephoneFormat) {
		telephoneNumberFormat = telephoneFormat;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	private String generateUsername(String firstName, String lastName) {
		if (randomBoolean()) {
			return lowerCase(stripAccents(firstName.substring(0, 1) + lastName));
		} else {
			return lowerCase(stripAccents(firstName + lastName.substring(0, 1)));
		}
	}
}


