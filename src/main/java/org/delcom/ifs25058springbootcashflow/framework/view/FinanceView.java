package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.delcom.ifs25058springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25058springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25058springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25058springbootcashflow.usecase.FinanceUseCase;

/** Dashboard transaksi milik akun yang sedang login (USER maupun ADMIN). */
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "transactions", layout = MainLayout.class)
@RouteAlias(value = "reports", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository users;
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;

    public FinanceView(IUserRepository users, FinanceUseCase finance, FinancePresenter presenter,
                       FileStorage storage) {
        this.users = users;
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        removeAll();
        add(new H2("Catatan Keuangan"),
                new TransactionPanel(finance, presenter, storage, user, user.id(), false));
    }
}
