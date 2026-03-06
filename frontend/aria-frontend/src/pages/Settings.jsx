import { Card } from "../ui/Card";
import { Button } from "../ui/Button";

export default function Settings() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold tracking-tight">Settings</h1>
        <p className="mt-1 text-sm text-white/60">Impostazioni profilo (mock).</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card>
          <div className="font-semibold">Profile</div>
          <div className="mt-2 text-sm text-white/70">Artist name, email, avatar…</div>
          <div className="mt-4">
            <Button variant="ghost">Edit</Button>
          </div>
        </Card>

        <Card>
          <div className="font-semibold">Authentication</div>
          <div className="mt-2 text-sm text-white/70">
            Password login / Google login (lo facciamo dopo).
          </div>
          <div className="mt-4 flex gap-2">
            <Button variant="ghost">Change password</Button>
            <Button variant="ghost">Connect Google</Button>
          </div>
        </Card>
      </div>
    </div>
  );
}