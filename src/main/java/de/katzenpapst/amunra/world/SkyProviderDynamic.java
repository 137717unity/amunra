package de.katzenpapst.amunra.world;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

public class SkyProviderDynamic extends IRenderHandler {
	
	private static final ResourceLocation overworldTexture = new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/gui/celestialbodies/earth.png");
    private static final ResourceLocation sunTexture = new ResourceLocation("textures/environment/sun.png");

    public int starList;
    public int glSkyList;
    public int glSkyList2;
    private float sunSize;
    
    // system to render in the sky
    protected SolarSystem curSystem;
    // the body to render it around
    protected CelestialBody curBody;
    // this is either the same as curBody, or it's parent, if its a moon
    protected Planet curBodyPlanet;
    // the distance of this body or it's parent from the sun
    protected float curBodyDistance;
	private float boxWidthHalf = 311;
    
    public SkyProviderDynamic(IGalacticraftWorldProvider worldProvider) {
    	this.sunSize = 2*worldProvider.getSolarSize();
    	curBody = worldProvider.getCelestialBody();
    	// find the current system
    	
    	if(curBody instanceof Planet) {
    		curBodyPlanet = ((Planet)curBody);
    		curSystem = curBodyPlanet.getParentSolarSystem();
    	} else if(curBody instanceof Moon) {
    		curBodyPlanet = ((Moon)curBody).getParentPlanet();
    		curSystem = curBodyPlanet.getParentSolarSystem();
    	} else {
    		// todo do somethign
    	}
    	curBodyDistance = curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance;
    	//curSystem = curBody.getPhaseShift(
        
    	int displayLists = GLAllocation.generateDisplayLists(3);
        this.starList = displayLists;
        this.glSkyList = displayLists + 1;
        this.glSkyList2 = displayLists + 2;

        // Bind stars to display list
        GL11.glPushMatrix();
        GL11.glNewList(this.starList, GL11.GL_COMPILE);
        this.renderStars();
        GL11.glEndList();
        GL11.glPopMatrix();

        final Tessellator tessellator = Tessellator.instance;
        GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
        final byte byte2 = 64;
        final int i = 256 / byte2 + 2;
        float f = 16F;

        for (int j = -byte2 * i; j <= byte2 * i; j += byte2)
        {
            for (int l = -byte2 * i; l <= byte2 * i; l += byte2)
            {
                tessellator.startDrawingQuads();
                tessellator.addVertex(j + 0, f, l + 0);
                tessellator.addVertex(j + byte2, f, l + 0);
                tessellator.addVertex(j + byte2, f, l + byte2);
                tessellator.addVertex(j + 0, f, l + byte2);
                tessellator.draw();
            }
        }

        GL11.glEndList();
        GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
        f = -16F;
        tessellator.startDrawingQuads();

        for (int k = -byte2 * i; k <= byte2 * i; k += byte2)
        {
            for (int i1 = -byte2 * i; i1 <= byte2 * i; i1 += byte2)
            {
                tessellator.addVertex(k + byte2, f, i1 + 0);
                tessellator.addVertex(k + 0, f, i1 + 0);
                tessellator.addVertex(k + 0, f, i1 + byte2);
                tessellator.addVertex(k + byte2, f, i1 + byte2);
            }
        }

        tessellator.draw();
        GL11.glEndList();
    }
    

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();
        Vec3 vec3 = world.getSkyColor(mc.renderViewEntity, partialTicks);
        float f1 = (float) vec3.xCoord;
        float f2 = (float) vec3.yCoord;
        float f3 = (float) vec3.zCoord;
        float f6;

        if (mc.gameSettings.anaglyph)
        {
            float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
            float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
            f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
            f1 = f4;
            f2 = f5;
            f3 = f6;
        }

        GL11.glColor3f(f1, f2, f3);
        Tessellator tessellator1 = Tessellator.instance;
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glColor3f(f1, f2, f3);
        GL11.glCallList(this.glSkyList);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        float f7;
        float f8;
        float f9;
        float f10;

        float f18 = world.getStarBrightness(partialTicks);

        if (f18 > 0.0F)
        {
            GL11.glColor4f(f18, f18, f18, f18);
            GL11.glCallList(this.starList);
        }

        float[] afloat = new float[4];
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        afloat[0] = 255 / 255.0F;
        afloat[1] = 194 / 255.0F;
        afloat[2] = 180 / 255.0F;
        afloat[3] = 0.3F;
        f6 = afloat[0];
        f7 = afloat[1];
        f8 = afloat[2];
        float f11;

        if (mc.gameSettings.anaglyph)
        {
            f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
            f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
            f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
            f6 = f9;
            f7 = f10;
            f8 = f11;
        }

        f18 = 1.0F - f18;
/*
		// small sun aura START
        tessellator1.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator1.setColorRGBA_F(f6 * f18, f7 * f18, f8 * f18, afloat[3] * 2 / f18);
        tessellator1.addVertex(0.0D, 100.0D, 0.0D);
        byte b0 = 16;
        tessellator1.setColorRGBA_F(afloat[0] * f18, afloat[1] * f18, afloat[2] * f18, 0.0F);

        
        // Render sun aura
        f10 = 20.0F;
        tessellator1.addVertex(-f10, 100.0D, -f10);
        tessellator1.addVertex(0, 100.0D, (double) -f10 * 1.5F);
        tessellator1.addVertex(f10, 100.0D, -f10);
        tessellator1.addVertex((double) f10 * 1.5F, 100.0D, 0);
        tessellator1.addVertex(f10, 100.0D, f10);
        tessellator1.addVertex(0, 100.0D, (double) f10 * 1.5F);
        tessellator1.addVertex(-f10, 100.0D, f10);
        tessellator1.addVertex((double) -f10 * 1.5F, 100.0D, 0);
        tessellator1.addVertex(-f10, 100.0D, -f10);

        tessellator1.draw(); 
        // small sun aura END
        */
        /*
        // large sun aura BEGIN
        tessellator1.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator1.setColorRGBA_F(f6 * f18, f7 * f18, f8 * f18, afloat[3] * f18);
        tessellator1.addVertex(0.0D, 100.0D, 0.0D);
        tessellator1.setColorRGBA_F(afloat[0] * f18, afloat[1] * f18, afloat[2] * f18, 0.0F);

        // Render larger sun aura
        
        f10 = 40.0F;
        tessellator1.addVertex(-f10, 100.0D, -f10);
        tessellator1.addVertex(0, 100.0D, (double) -f10 * 1.5F);
        tessellator1.addVertex(f10, 100.0D, -f10);
        tessellator1.addVertex((double) f10 * 1.5F, 100.0D, 0);
        tessellator1.addVertex(f10, 100.0D, f10);
        tessellator1.addVertex(0, 100.0D, (double) f10 * 1.5F);
        tessellator1.addVertex(-f10, 100.0D, f10);
        tessellator1.addVertex((double) -f10 * 1.5F, 100.0D, 0);
        tessellator1.addVertex(-f10, 100.0D, -f10);

        tessellator1.draw();
        // large sun aura END
        */
        GL11.glPopMatrix();
        GL11.glShadeModel(GL11.GL_FLAT);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glPushMatrix();
        f7 = 0.0F;
        f8 = 0.0F;
        f9 = 0.0F;
        GL11.glTranslatef(f7, f8, f9);
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        // rotates the sky by the celestial angle on the x axis
        // this seems to mean that the x-axis is the rotational axis of the planet
        // does the sun move from -z to z or the other way round?
        GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
       
        // so at this point, I'm where the sun is supposed to be. This is where I have to start.
        
        // Render sun
        renderSystem(partialTicks, world, tessellator1, mc);
        
        
        
        /*
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);        
        //Some blanking to conceal the stars
        f10 = this.sunSize / 3.5F;
        tessellator1.startDrawingQuads();
        tessellator1.addVertex(-f10, 99.9D, -f10);
        tessellator1.addVertex(f10, 99.9D, -f10);
        tessellator1.addVertex(f10, 99.9D, f10);
        tessellator1.addVertex(-f10, 99.9D, f10);
        tessellator1.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        f10 = this.sunSize;
        mc.renderEngine.bindTexture(this.sunTexture);
        tessellator1.startDrawingQuads();
        tessellator1.addVertexWithUV(-f10, 100.0D, -f10, 0.0D, 0.0D);
        tessellator1.addVertexWithUV(f10, 100.0D, -f10, 1.0D, 0.0D);
        tessellator1.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
        tessellator1.addVertexWithUV(-f10, 100.0D, f10, 0.0D, 1.0D);
        tessellator1.draw();*/

        // reset rotation. I have no idea why oO
        // GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        
        
       
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(0.0F, 0.0F, 0.0F);
        double d0 = mc.thePlayer.getPosition(partialTicks).yCoord - world.getHorizon();

        if (d0 < 0.0D)
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 12.0F, 0.0F);
            GL11.glCallList(this.glSkyList2);
            GL11.glPopMatrix();
            f8 = 1.0F;
            f9 = -((float) (d0 + 65.0D));
            f10 = -f8;
            tessellator1.startDrawingQuads();
            tessellator1.setColorRGBA_I(0, 255);
            tessellator1.addVertex(-f8, f9, f8);
            tessellator1.addVertex(f8, f9, f8);
            tessellator1.addVertex(f8, f10, f8);
            tessellator1.addVertex(-f8, f10, f8);
            tessellator1.addVertex(-f8, f10, -f8);
            tessellator1.addVertex(f8, f10, -f8);
            tessellator1.addVertex(f8, f9, -f8);
            tessellator1.addVertex(-f8, f9, -f8);
            tessellator1.addVertex(f8, f10, -f8);
            tessellator1.addVertex(f8, f10, f8);
            tessellator1.addVertex(f8, f9, f8);
            tessellator1.addVertex(f8, f9, -f8);
            tessellator1.addVertex(-f8, f9, -f8);
            tessellator1.addVertex(-f8, f9, f8);
            tessellator1.addVertex(-f8, f10, f8);
            tessellator1.addVertex(-f8, f10, -f8);
            tessellator1.addVertex(-f8, f10, -f8);
            tessellator1.addVertex(-f8, f10, f8);
            tessellator1.addVertex(f8, f10, f8);
            tessellator1.addVertex(f8, f10, -f8);
            tessellator1.draw();
        }

        if (world.provider.isSkyColored())
        {
            GL11.glColor3f(f1 * 0.2F + 0.04F, f2 * 0.2F + 0.04F, f3 * 0.6F + 0.1F);
        }
        else
        {
            GL11.glColor3f(f1, f2, f3);
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -((float) (d0 - 16.0D)), 0.0F);
        GL11.glCallList(this.glSkyList2);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        
        
	}
	
	protected void renderSystem(float partialTicks, WorldClient world, Tessellator tess, Minecraft mc) {
		// assume we are at the position of the sun
		
		GL11.glPushMatrix();
		// render the sun first
		GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);        
        //Some blanking to conceal the stars
        float f10 = this.sunSize / 3.5F;
        tess.startDrawingQuads();
        tess.addVertex(-f10, 99.9D, -f10);
        tess.addVertex(f10, 99.9D, -f10);
        tess.addVertex(f10, 99.9D, f10);
        tess.addVertex(-f10, 99.9D, f10);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        f10 = this.sunSize;
        mc.renderEngine.bindTexture(this.curSystem.getMainStar().getBodyIcon());
        tess.startDrawingQuads();
        tess.addVertexWithUV(-f10, 100.0D, -f10, 0.0D, 0.0D);
        tess.addVertexWithUV(f10, 100.0D, -f10, 1.0D, 0.0D);
        tess.addVertexWithUV(f10, 100.0D, f10, 1.0D, 1.0D);
        tess.addVertexWithUV(-f10, 100.0D, f10, 0.0D, 1.0D);
        tess.draw();
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //renderHalfArch(tess);
        
        
        
        long curWorldTime = world.getWorldTime();
        // get my own angle
        double curBodyOrbitalAngle = getOrbitalAngle(curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance, curBodyPlanet.getPhaseShift(), curWorldTime , partialTicks);
        
        
        
        
        // now render planets. wait wat. you can't just iterate through all the planets in a system?!
        int i=0;
        for (Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
        	// oh well I hope this doesn't kill the performance
        	if(planet.getParentSolarSystem() != curSystem) {
        		continue;
        	}
        	float dist = planet.getRelativeDistanceFromCenter().unScaledDistance;
        	// for now, disregard everything beyond my orbit
        	if(dist > curBodyDistance) {
        		// continue;
        	}
        	// try do my own
        	if(dist == curBodyDistance) {
            	// so seems like the skybox is some 622 across
            	//i++;
        		continue;
        	}
        	// orbital angle of the planet
        	double curOrbitalAngle = getOrbitalAngle(planet.getRelativeOrbitTime(), planet.getPhaseShift(), curWorldTime, partialTicks);
        	// but I need it relative to curOrbitalAngle, or actually to curOrbitalAngle rotated by 180�,
        	// just because that's how I calculated that stuff
        	
        	curOrbitalAngle -= (Math.PI*2-curBodyOrbitalAngle);
        	
        	
        	
        	// angle between connection line curBody<-->sun and planet<-->sun
        	double innerAngle = Math.PI-curOrbitalAngle;
        	
        	// distance between curBody<-->planet, also needed for scaling
        	double distanceToPlanet = getDistanceToBody(innerAngle, dist);
        	
        	double projectedAngle = projectAngle(innerAngle, dist, distanceToPlanet);
        	
        	renderPlanetByAngle(tess, planet.getBodyIcon(), (float)projectedAngle, 0, 1.0F / (float)distanceToPlanet);
        	
        }
        
	}
	
	protected double getOrbitalAngle(double relOrbitTime, double phaseShift, long worldTime, double partialTicks) {
		final long yearFactor = 24000L;
		
		double curYearLength = relOrbitTime * yearFactor;
		int j = (int)(worldTime % (long)curYearLength);
    	double orbitPos = (j + partialTicks) / curYearLength - 0.25F; 
    	return orbitPos*2*Math.PI + phaseShift;
	}
	
	private double getDistanceToBody(double innerAngle, double otherBodyDistance) {
		return Math.sqrt(
				Math.pow(otherBodyDistance, 2) + 
				Math.pow(curBodyDistance, 2) - 
				2 * otherBodyDistance * curBodyDistance * Math.cos(innerAngle));
	}
	
	
	
	/**
	 * Should convert an angle around the sun into an angle around this body
	 * 
	 * 
	 * @param innerAngle				in radians, the angle between curBody<-->sun and otherBody<-->sun
	 * @param otherBodyDistance			other body's orbital radius
	 * @param distFromThisToOtherBody	
	 * @return
	 */
	private double projectAngle(double innerAngle, double otherBodyDistance, double distFromThisToOtherBody) {
		// omg now do dark mathemagic
		/*if(angleAroundSun < 0) {
			angleAroundSun = Math.PI*2+angleAroundSun;
		}*/
		//double beta = Math.PI-angleAroundSun;
		double sinBeta = Math.sin(innerAngle);
		
		/*double distFromThisToOtherBody = Math.sqrt(
					Math.pow(otherBodyDistance, 2) + 
					Math.pow(curBodyDistance, 2) - 
					2 * otherBodyDistance * curBodyDistance * Math.cos(innerAngle)
				);*/
		double angleAroundCurBody = Math.asin(
					otherBodyDistance * sinBeta / distFromThisToOtherBody
				);
		
		if ( curBodyDistance > otherBodyDistance) {
			return angleAroundCurBody;
		}
		
		// now fix this angle...
		// for this, I need the third angle, too
		double delta = Math.asin(sinBeta / distFromThisToOtherBody * curBodyDistance);
		
		
		double angleSum = innerAngle+delta+angleAroundCurBody;
		double otherAngleSum =innerAngle+delta+(Math.PI-angleAroundCurBody);
		if(Math.abs(Math.abs(angleSum)/Math.PI - 1) < 0.001) {
			// aka angleSUm = 180 or -180
			return angleAroundCurBody;
		} else {
			return Math.PI-angleAroundCurBody;
		}
	}
	

	
	private void renderPlanetByAngle(Tessellator tessellator1, ResourceLocation texture, float angle, float offset, float scale) {
		// we start at the sun
		GL11.glPushMatrix();
		//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);   // change this for your colour
		//GL11.glLineWidth(2.0F);
		// rotate on x
		GL11.glRotatef((float) (angle/Math.PI*180), 1.0F, 0.0F, 0.0F);
		
		//renderPlanet(tessellator1,texture,0,offset);
		// BEGIN
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
		FMLClientHandler.instance().getClient().renderEngine.bindTexture(texture);
        tessellator1.startDrawingQuads();
//        final float scale = 2;
      
        
        
        
        
        // go to the position
        
        GL11.glTranslatef(offset, 91.0F, 0);
        
        
        
        tessellator1.addVertexWithUV(-scale, 0, -scale, 0, 0);
        tessellator1.addVertexWithUV(scale, 0, -scale, 1, 0);
        tessellator1.addVertexWithUV(scale, 0, scale, 1, 1);
        tessellator1.addVertexWithUV(-scale, 0, scale, 0, 1);
        
  
        tessellator1.draw();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        
		// END
		
		
		
		GL11.glPopMatrix();
	}

	/*private void renderOrbit(Tessellator tessellator1 , float scale) {
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);   // change this for your colour
		GL11.glLineWidth(2.0F);
        tessellator1.startDrawing(GL11.GL_LINE_LOOP);
   
        float x = -scale;
        float y = scale;
        float temp;
        final float theta = (float) (2 * Math.PI / 90);
        final float cos = (float) Math.cos(theta);
        final float sin = (float) Math.sin(theta);
        for (int i = 0; i < 90; i++)
        {
        	tessellator1.addVertex(x, -90F, -y);

            temp = x;
            x = cos * x - sin * y;
            y = sin * temp + cos * y;
        }
        
        tessellator1.draw();
	}*/
	
	private void renderStars()
    {
        final Random rand = new Random(10842L);
        final Tessellator var2 = Tessellator.instance;
        var2.startDrawingQuads();

        for (int starIndex = 0; starIndex < (6000); ++starIndex)
        {
            double var4 = rand.nextFloat() * 2.0F - 1.0F;
            double var6 = rand.nextFloat() * 2.0F - 1.0F;
            double var8 = rand.nextFloat() * 2.0F - 1.0F;
            final double var10 = 0.15F + rand.nextFloat() * 0.1F;
            double var12 = var4 * var4 + var6 * var6 + var8 * var8;

            if (var12 < 1.0D && var12 > 0.01D)
            {
                var12 = 1.0D / Math.sqrt(var12);
                var4 *= var12;
                var6 *= var12;
                var8 *= var12;
                final double var14 = var4 * 100.0D;//(ConfigManagerCore.moreStars ? rand.nextDouble() * 150D + 130D : 100.0D);
                final double var16 = var6 * 100.0D;//(ConfigManagerCore.moreStars ? rand.nextDouble() * 150D + 130D : 100.0D);
                final double var18 = var8 * 100.0D;//(ConfigManagerCore.moreStars ? rand.nextDouble() * 150D + 130D : 100.0D);
                final double var20 = Math.atan2(var4, var8);
                final double var22 = Math.sin(var20);
                final double var24 = Math.cos(var20);
                final double var26 = Math.atan2(Math.sqrt(var4 * var4 + var8 * var8), var6);
                final double var28 = Math.sin(var26);
                final double var30 = Math.cos(var26);
                final double var32 = rand.nextDouble() * Math.PI * 2.0D;
                final double var34 = Math.sin(var32);
                final double var36 = Math.cos(var32);

                for (int var38 = 0; var38 < 4; ++var38)
                {
                    final double var39 = 0.0D;
                    final double var41 = ((var38 & 2) - 1) * var10;
                    final double var43 = ((var38 + 1 & 2) - 1) * var10;
                    final double var47 = var41 * var36 - var43 * var34;
                    final double var49 = var43 * var36 + var41 * var34;
                    final double var53 = var47 * var28 + var39 * var30;
                    final double var55 = var39 * var28 - var47 * var30;
                    final double var57 = var55 * var22 - var49 * var24;
                    final double var61 = var49 * var22 + var55 * var24;
                    var2.addVertex(var14 + var57, var16 + var53, var18 + var61);
                }
            }
        }

        var2.draw();
    }

    private Vec3 getCustomSkyColor()
    {
        return Vec3.createVectorHelper(0.26796875D, 0.1796875D, 0.0D);
    }

    public float getSkyBrightness(float par1)
    {
        final float var2 = FMLClientHandler.instance().getClient().theWorld.getCelestialAngle(par1);
        float var3 = 1.0F - (MathHelper.sin(var2 * (float) Math.PI * 2.0F) * 2.0F + 0.25F);

        if (var3 < 0.0F)
        {
            var3 = 0.0F;
        }

        if (var3 > 1.0F)
        {
            var3 = 1.0F;
        }

        return var3 * var3 * 1F;
    }
}