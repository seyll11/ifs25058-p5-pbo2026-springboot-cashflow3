package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs00000springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs00000springbootcashflow.usecase.AuthUseCase;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository users;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Ulangi password");
    private final Button registerButton = new Button("Daftar");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository users) {
        this.authUseCase = authUseCase;
        this.users = users;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        fullName.setWidthFull();
        username.setWidthFull();
        password.setWidthFull();
        confirm.setWidthFull();
        username.setHelperText("3-50 karakter: huruf, angka, titik, _ atau -");
        password.setHelperText("Minimal 6 karakter");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickShortcut(Key.ENTER);
        registerButton.addClickListener(e -> submit());
        progress.setIndeterminate(true);
        progress.setVisible(false);

        PageCard card = new PageCard(
                new H2("Buat Akun"),
                fullName, username, password, confirm, progress, registerButton,
                new Paragraph(new Span("Sudah punya akun? "), new RouterLink("Masuk", LoginView.class)));
        card.addClassName("cf-auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void submit() {
        if (!password.getValue().equals(confirm.getValue())) {
            Notifier.error("Konfirmasi password tidak sama");
            return;
        }
        setBusy(true);
        try {
            authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            Notifier.success("Registrasi berhasil, silakan masuk");
            UI.getCurrent().navigate(LoginView.class);
        } catch (DomainException ex) {
            Notifier.error(ex.getMessage());
        } finally {
            setBusy(false);
        }
    }

    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        registerButton.setEnabled(!busy);
    }
}
