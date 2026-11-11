package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
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
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25058springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25058springbootcashflow.usecase.AuthUseCase;

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository users;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository users) {
        this.authUseCase = authUseCase;
        this.users = users;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setId("login-username");
        username.setWidthFull();
        username.setAutofocus(true);
        password.setId("login-password");
        password.setWidthFull();
        loginButton.setId("login-submit");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(e -> submit());
        progress.setIndeterminate(true);
        progress.setVisible(false);

        PageCard card = new PageCard(
                new H2("Cashflow"),
                new Paragraph("Masuk untuk mencatat pemasukan dan pengeluaran Anda."),
                username, password, progress, loginButton,
                new Paragraph(new com.vaadin.flow.component.html.Span("Belum punya akun? "),
                        new RouterLink("Daftar", RegisterView.class)));
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
        setBusy(true);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException ex) {
            Notifier.error(ex.getMessage());
            password.clear();
        } finally {
            setBusy(false);
        }
    }

    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        loginButton.setEnabled(!busy);
    }
}
