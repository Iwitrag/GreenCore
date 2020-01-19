package cz.iwitrag.greencore.premium.handlers;

public class NotImplementedHandler extends Handler {

    @Override
    public void handle() {
        solve(Result.FAILURE, "Tato služba zatím není funkční :(", "Service not implemented");
    }
}
